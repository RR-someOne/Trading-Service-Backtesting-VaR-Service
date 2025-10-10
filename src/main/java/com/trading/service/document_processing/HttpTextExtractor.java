package com.trading.service.document_processing;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

/** Minimal HTTP text extractor. For HTML/PDF, integrate a parser (jsoup/pdfbox) in the future. */
public class HttpTextExtractor implements DocumentExtractor {
  private final HttpClient client = HttpClient.newHttpClient();

  @Override
  public FinancialDocument extract(String symbol, DocumentType type, URI source)
      throws IOException {
    try {
      HttpRequest req = HttpRequest.newBuilder(source).GET().build();
      HttpResponse<byte[]> resp = client.send(req, HttpResponse.BodyHandlers.ofByteArray());
      String body = new String(resp.body(), StandardCharsets.UTF_8);
      return new FinancialDocument(
          UUID.randomUUID().toString(), symbol, type, Instant.now(), source.toString(), body);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Interrupted", e);
    }
  }
}
