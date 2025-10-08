package com.trading.service.data.ingestion.timeseries;

import com.trading.service.data.ingestion.model.BarEvent;
import com.trading.service.data.ingestion.model.MarketDataEvent;

/** Abstraction for writing events to a time-series store. */
public interface TimeSeriesWriter extends AutoCloseable {
  void writeTick(MarketDataEvent e);

  void writeBar(BarEvent e);

  @Override
  void close();
}
