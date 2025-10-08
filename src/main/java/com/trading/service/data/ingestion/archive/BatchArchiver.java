package com.trading.service.data.ingestion.archive;

import com.trading.service.data.ingestion.model.BarEvent;
import com.trading.service.data.ingestion.model.MarketDataEvent;

/** Interface for batching events to cold storage (e.g., S3). */
public interface BatchArchiver extends AutoCloseable {
  void acceptTick(MarketDataEvent e);

  void acceptBar(BarEvent e);

  void flush();

  @Override
  void close();
}
