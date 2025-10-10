package com.trading.service.api;

public interface SecretsProvider {
  String getSecret(String key) throws Exception;
}
