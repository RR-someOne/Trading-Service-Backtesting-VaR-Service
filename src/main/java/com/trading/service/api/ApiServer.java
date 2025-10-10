package com.trading.service.api;

import com.trading.service.api.secrets.AwsSecretsManagerProvider;
import com.trading.service.api.secrets.VaultSecretsProvider;
import java.net.URI;
import software.amazon.awssdk.regions.Region;

public class ApiServer {
  public static void main(String[] args) throws Exception {
    int restPort = Integer.parseInt(System.getenv().getOrDefault("API_REST_PORT", "8080"));
    int grpcPort = Integer.parseInt(System.getenv().getOrDefault("API_GRPC_PORT", "9090"));

    // Resolve API key
    String apiKey = System.getenv("API_KEY");
    String secretsBackend = System.getenv().getOrDefault("SECRETS_BACKEND", "env");
    if ((apiKey == null || apiKey.isEmpty()) && "aws".equalsIgnoreCase(secretsBackend)) {
      String secretId = System.getenv().getOrDefault("SECRETS_KEY", "trading/api-key");
      try (AwsSecretsManagerProvider aws = new AwsSecretsManagerProvider(Region.AWS_GLOBAL)) {
        apiKey = aws.getSecret(secretId);
      }
    } else if ((apiKey == null || apiKey.isEmpty()) && "vault".equalsIgnoreCase(secretsBackend)) {
      String base = System.getenv().getOrDefault("VAULT_ADDR", "http://127.0.0.1:8200");
      String token = System.getenv().getOrDefault("VAULT_TOKEN", "");
      String path = System.getenv().getOrDefault("SECRETS_KEY", "secret/data/trading/api-key");
      VaultSecretsProvider vp = new VaultSecretsProvider(URI.create(base), token);
      apiKey = vp.getSecret(path); // raw JSON from Vault KV
    }

    // Start servers
    try (RestApiServer rest = new RestApiServer(restPort, apiKey);
        GrpcApiServer grpc = new GrpcApiServer(grpcPort)) {
      grpc.start();
      System.out.println("REST started on " + restPort + ", gRPC on " + grpcPort);
      Thread.currentThread().join();
    }
  }
}
