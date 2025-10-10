package com.trading.service.api.secrets;

import com.trading.service.api.SecretsProvider;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class VaultSecretsProvider implements SecretsProvider {
  private final HttpClient http;
  private final URI base;
  private final String token;

  public VaultSecretsProvider(URI base, String token) {
    this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    this.base = base;
    this.token = token;
  }

  @Override
  public String getSecret(String key) throws Exception {
    // Assumes KV v2 at secret/data/<path>
    URI uri = base.resolve("/v1/" + key);
    HttpRequest req =
        HttpRequest.newBuilder(uri)
            .header("X-Vault-Token", token)
            .GET()
            .timeout(Duration.ofSeconds(5))
            .build();
    HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
    if (resp.statusCode() / 100 != 2) {
      throw new RuntimeException("Vault error: " + resp.statusCode() + " - " + resp.body());
    }
    return resp.body();
  }
}
