package com.trading.service.data.ingestion.connector;

import com.trading.service.data.ingestion.model.BarEvent;
import com.trading.service.data.ingestion.model.MarketDataEvent;
import java.util.function.Consumer;

/** Base interface for any market data connector (WebSocket, REST polling, FIX, etc.). */
public interface MarketDataConnector extends AutoCloseable {
  void start();

  boolean isRunning();

  void setTickHandler(Consumer<MarketDataEvent> handler);

  void setBarHandler(Consumer<BarEvent> handler);

  @Override
  void close();

  String name();
}
