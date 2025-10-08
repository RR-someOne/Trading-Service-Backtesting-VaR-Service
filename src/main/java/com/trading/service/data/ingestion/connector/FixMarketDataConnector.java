package com.trading.service.data.ingestion.connector;

import com.trading.service.data.ingestion.model.BarEvent;
import com.trading.service.data.ingestion.model.MarketDataEvent;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/** Placeholder for FIX protocol connector (not implemented). */
@SuppressWarnings("unused")
public class FixMarketDataConnector implements MarketDataConnector, RawMessageCapable {
  private final AtomicBoolean running = new AtomicBoolean();
  private volatile Consumer<MarketDataEvent> tickHandler = e -> {};
  private volatile Consumer<BarEvent> barHandler = e -> {};
  private volatile Consumer<String> rawConsumer = s -> {};

  @Override
  public void start() {
    running.set(true);
  }

  @Override
  public boolean isRunning() {
    return running.get();
  }

  @Override
  public void setTickHandler(Consumer<MarketDataEvent> handler) {
    this.tickHandler = handler;
  }

  @Override
  public void setBarHandler(Consumer<BarEvent> handler) {
    this.barHandler = handler;
  }

  @Override
  public void setRawMessageConsumer(Consumer<String> consumer) {
    this.rawConsumer = consumer;
  }

  @Override
  public void close() {
    running.set(false);
  }

  @Override
  public String name() {
    return "fix";
  }
}
