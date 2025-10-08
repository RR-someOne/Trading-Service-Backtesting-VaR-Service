package com.trading.service.data.ingestion.publish;

import com.trading.service.data.ingestion.model.BarEvent;
import com.trading.service.data.ingestion.model.MarketDataEvent;

public interface Publisher extends AutoCloseable {
  void publishTick(MarketDataEvent evt);

  void publishBar(BarEvent evt);

  @Override
  void close();
}
