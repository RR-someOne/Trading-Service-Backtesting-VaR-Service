package com.trading.service.data;

import com.trading.service.net.HttpClientConfig;
import com.trading.service.net.HttpClientFactory;
import java.io.IOException;
import java.net.http.HttpClient;
import java.time.Duration;

/** Adapter for market data ingestion and retrieval with high-performance HTTP access. */
public class DataService {
  private final HttpClient httpClient;

  public DataService() {
    this(
        HttpClientFactory.shared(
            HttpClientConfig.builder().connectTimeout(Duration.ofSeconds(3)).build()));
  }

  public DataService(HttpClient client) {
    this.httpClient = client;
  }

  /** Fetch a JSON endpoint with caching and retries; returns raw body for maximum speed. */
  public String fetchJson(String url) throws IOException {
    try {
      return HttpClientFactory.getJson(httpClient, url);
    } catch (RuntimeException e) {
      if (e.getCause() instanceof IOException) {
        throw (IOException) e.getCause();
      }
      throw e;
    }
  }
}
