package com.trading.service.api.secrets;

import com.trading.service.api.SecretsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;

public class AwsSecretsManagerProvider implements SecretsProvider, AutoCloseable {
  private final SecretsManagerClient client;

  public AwsSecretsManagerProvider(Region region) {
    this.client =
        SecretsManagerClient.builder()
            .region(region)
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build();
  }

  @Override
  public String getSecret(String key) {
    return client.getSecretValue(GetSecretValueRequest.builder().secretId(key).build())
        .secretString();
  }

  @Override
  public void close() {
    client.close();
  }
}
