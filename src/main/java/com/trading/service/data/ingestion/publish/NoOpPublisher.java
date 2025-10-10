package com.trading.service.data.ingestion.publish;

import com.trading.service.data.ingestion.model.BarEvent;
import com.trading.service.data.ingestion.model.MarketDataEvent;

/**
 * Development-friendly publisher that drops all events. Use when Kafka is not available (e.g.,
 * local runs) by setting KAFKA_ENABLED=false.
 */
public class NoOpPublisher implements Publisher {
  @Override
  public void publishTick(MarketDataEvent evt) {
    // no-op
  }

  @Override
  public void publishBar(BarEvent evt) {
    // no-op
  }

  @Override
  public void close() {
    // no-op
  }
}
