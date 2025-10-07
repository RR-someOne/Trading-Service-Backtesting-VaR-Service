package com.trading.service.net;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * High-performance HTTP client factory leveraging JDK HttpClient with connection pooling,
 * High-performance HTTP client factory leveraging JDK HttpClient with:
 *
 * <p>• Connection pooling (built-in)
 *
 * <p>• HTTP/1.1 + HTTP/2 where available
 *
 * <p>• Short‑lived cached responses
 *
 * <p>• Basic retry logic
 *
 * <p>• Synchronous & asynchronous APIs
 */
public final class HttpClientFactory {
  private static final HttpClient sharedStatic =
      HttpClient.newBuilder()
          .connectTimeout(Duration.ofSeconds(5))
          .followRedirects(HttpClient.Redirect.NORMAL)
          .version(HttpClient.Version.HTTP_2)
          .build();

  // Simple TTL cache (non-LRU). When size exceeds capacity, oldest entries are purged.
  private static final int CACHE_CAPACITY = 256;
  private static final Map<String, Cached> CACHE = new HashMap<>();

  private HttpClientFactory() {}

  public static HttpClient shared(HttpClientConfig cfg) {
    // For now ignore custom config in pooled instance except HTTP/2 flag.
    return sharedStatic;
  }

  public static String getJson(HttpClient client, String url) throws IOException {
    return cachedFetch(client, url, true);
  }

  public static CompletableFuture<String> getJsonAsync(HttpClient client, String url) {
    // No cache on async path to keep it simple; could be added similarly.
    HttpRequest req =
        HttpRequest.newBuilder(URI.create(url)).GET().timeout(Duration.ofSeconds(10)).build();
    return client.sendAsync(req, BodyHandlers.ofString()).thenApply(HttpClientFactory::ensure2xx);
  }

  private static String cachedFetch(HttpClient client, String url, boolean retry)
      throws IOException {
    long now = System.currentTimeMillis();
    Cached existing;
    synchronized (CACHE) {
      existing = CACHE.get(url);
      if (existing != null && (now - existing.storedAt) < 15_000) {
        return existing.body;
      }
    }
    String body = executeWithRetries(() -> doFetch(client, url), retry ? 2 : 1);
    synchronized (CACHE) {
      CACHE.put(url, new Cached(body, now));
      if (CACHE.size() > CACHE_CAPACITY) {
        // Purge oldest 10%.
        List<Cached> entries = new ArrayList<>(CACHE.values());
        Collections.sort(entries, Comparator.comparingLong(c -> c.storedAt));
        int toRemove = Math.max(1, CACHE_CAPACITY / 10);
        for (int i = 0; i < toRemove && i < entries.size(); i++) {
          Cached ev = entries.get(i);
          // linear scan to find key (small N relative to capacity)
          for (Map.Entry<String, Cached> e : CACHE.entrySet()) {
            if (e.getValue() == ev) {
              CACHE.remove(e.getKey());
              break;
            }
          }
        }
      }
    }
    return body;
  }

  private static String doFetch(HttpClient client, String url) throws IOException {
    HttpRequest req =
        HttpRequest.newBuilder(URI.create(url))
            .GET()
            .timeout(Duration.ofSeconds(8))
            .header("Accept", "application/json")
            .build();
    try {
      HttpResponse<String> resp = client.send(req, BodyHandlers.ofString());
      ensure2xx(resp);
      return resp.body();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Interrupted", e);
    }
  }

  private static <T> T executeWithRetries(IOCall<T> call, int maxAttempts) throws IOException {
    IOException last = null;
    for (int attempt = 1; attempt <= maxAttempts; attempt++) {
      try {
        return call.run();
      } catch (IOException ex) {
        last = ex;
      }
    }
    throw last;
  }

  private static String ensure2xx(HttpResponse<String> r) {
    if (r.statusCode() / 100 != 2) {
      throw new IllegalStateException("HTTP status " + r.statusCode());
    }
    return r.body();
  }

  private static final class Cached {
    final String body;
    final long storedAt;

    Cached(String body, long storedAt) {
      this.body = body;
      this.storedAt = storedAt;
    }
  }

  @FunctionalInterface
  private interface IOCall<T> {
    T run() throws IOException;
  }
}
