package com.trading.service.data.bus;

import com.trading.service.data.ingestion.model.MarketDataEvent;

/**
 * Abstraction for publishing tick-level market data events to a bus (Kafka, Pulsar, in-memory,
 * etc).
 */
public interface TickEventBus extends AutoCloseable {
  void publish(MarketDataEvent event);

  @Override
  void close();
}
