package com.trading.service.data.bus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trading.service.data.ingestion.model.MarketDataEvent;
import java.util.concurrent.CompletableFuture;

/** Placeholder Pulsar implementation (logic stubbed to avoid external dependency for now). */
public class PulsarTickEventBus implements TickEventBus {
  private final ObjectMapper mapper = new ObjectMapper();
  private final String topic;

  public PulsarTickEventBus(String topic) {
    this.topic = topic;
  }

  @Override
  public void publish(MarketDataEvent event) {
    // In a real implementation, use Pulsar client. Here we simulate async no-op send.
    CompletableFuture.runAsync(() -> toJson(event));
  }

  private String toJson(Object o) {
    try {
      return mapper.writeValueAsString(o);
    } catch (JsonProcessingException e) {
      return "{}";
    }
  }

  @Override
  public void close() {}
}
