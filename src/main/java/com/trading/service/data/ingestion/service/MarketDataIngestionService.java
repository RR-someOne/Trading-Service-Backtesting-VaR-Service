package com.trading.service.data.ingestion.service;

import com.trading.service.data.ingestion.archive.BatchArchiver;
import com.trading.service.data.ingestion.config.IngestionConfig;
import com.trading.service.data.ingestion.connector.MarketDataConnector;
import com.trading.service.data.ingestion.model.BarEvent;
import com.trading.service.data.ingestion.model.MarketDataEvent;
import com.trading.service.data.ingestion.publish.IngressDispatcher;
import com.trading.service.data.ingestion.publish.Publisher;
import com.trading.service.data.ingestion.timeseries.TimeSeriesWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Orchestrates connectors and dispatch pipeline. */
@SuppressWarnings("unused")
public class MarketDataIngestionService implements AutoCloseable {
  private final IngestionConfig config; // retained for future configuration lookups
  private final IngressDispatcher dispatcher;
  private final List<MarketDataConnector> connectors = new ArrayList<>();

  public MarketDataIngestionService(
      IngestionConfig cfg, Publisher publisher, TimeSeriesWriter writer, BatchArchiver archiver) {
    this.config = Objects.requireNonNull(cfg);
    this.dispatcher =
        new IngressDispatcher(publisher, writer, archiver, cfg.getDispatcherQueueCapacity());
    this.dispatcher.start();
  }

  public void registerConnector(MarketDataConnector c) {
    c.setTickHandler(this::onTick);
    c.setBarHandler(this::onBar);
    connectors.add(c);
  }

  private void onTick(MarketDataEvent e) {
    dispatcher.submitTick(e);
  }

  private void onBar(BarEvent e) {
    dispatcher.submitBar(e);
  }

  public void startAll() {
    connectors.forEach(MarketDataConnector::start);
  }

  @Override
  public void close() {
    connectors.forEach(
        c -> {
          try {
            c.close();
          } catch (Exception ignored) {
          }
        });
    try {
      dispatcher.close();
    } catch (Exception ignored) {
    }
  }
}
