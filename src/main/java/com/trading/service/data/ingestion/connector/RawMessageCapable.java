package com.trading.service.data.ingestion.connector;

import java.util.function.Consumer;

/** Capability marker for connectors that can forward raw textual payloads. */
public interface RawMessageCapable {
  void setRawMessageConsumer(Consumer<String> consumer);
}
