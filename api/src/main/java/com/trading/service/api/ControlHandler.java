package com.trading.service.api;

import java.util.Map;

public interface ControlHandler {
  void startIngestion() throws Exception;

  void stopIngestion() throws Exception;

  Map<String, Object> status();
}
