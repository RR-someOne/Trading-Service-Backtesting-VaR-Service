package com.trading.service.data.ingestion.service;

import com.trading.service.data.ingestion.archive.BatchArchiver;
import com.trading.service.data.ingestion.config.IngestionConfig;
import com.trading.service.data.ingestion.connector.MarketDataConnector;
import com.trading.service.data.ingestion.connector.RawMessageCapable;
import com.trading.service.data.ingestion.gateway.IngestionGateway;
import com.trading.service.data.ingestion.model.BarEvent;
import com.trading.service.data.ingestion.model.MarketDataEvent;
import com.trading.service.data.ingestion.publish.IngressDispatcher;
import com.trading.service.data.ingestion.publish.Publisher;
import com.trading.service.data.ingestion.timeseries.TimeSeriesWriter;
import com.trading.service.persistence.featurestore.FeatureStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Orchestrates connectors and dispatch pipeline. */
@SuppressWarnings("unused")
public class MarketDataIngestionService implements AutoCloseable {
  private final IngestionConfig config; // retained for future configuration lookups
  private final IngressDispatcher dispatcher;
  private final List<MarketDataConnector> connectors = new ArrayList<>();
  private IngestionGateway gateway; // optional
  private FeatureStore featureStore; // optional

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
    if (gateway != null && c instanceof RawMessageCapable) {
      RawMessageCapable rmc = (RawMessageCapable) c;
      rmc.setRawMessageConsumer(gateway::onRaw);
    }
    connectors.add(c);
  }

  public void attachGateway(IngestionGateway gateway) {
    this.gateway = gateway;
    // retrofit existing connectors if they support raw
    for (MarketDataConnector c : connectors) {
      if (c instanceof RawMessageCapable) {
        RawMessageCapable rmc = (RawMessageCapable) c;
        rmc.setRawMessageConsumer(gateway::onRaw);
      }
    }
  }

  private void onTick(MarketDataEvent e) {
    dispatcher.submitTick(e);
  }

  private void onBar(BarEvent e) {
    // Ingest feature store (close price at bar end) before dispatch
    if (featureStore != null) {
      try {
        featureStore.ingestClose(e.symbol(), e.endEpochMillis(), e.close());
      } catch (Exception ignored) {
      }
    }
    dispatcher.submitBar(e);
  }

  public void startAll() {
    connectors.forEach(MarketDataConnector::start);
  }

  public void stopAll() {
    connectors.forEach(
        c -> {
          try {
            c.close();
          } catch (Exception ignored) {
          }
        });
  }

  public boolean isRunning() {
    for (MarketDataConnector c : connectors) {
      try {
        if (c.isRunning()) return true;
      } catch (Exception ignored) {
      }
    }
    return false;
  }

  // Gateway submission hooks
  public void submitTick(MarketDataEvent e) {
    dispatcher.submitTick(e);
  }

  public void submitBar(BarEvent e) {
    if (featureStore != null) {
      try {
        featureStore.ingestClose(e.symbol(), e.endEpochMillis(), e.close());
      } catch (Exception ignored) {
      }
    }
    dispatcher.submitBar(e);
  }

  // Feature Store wiring
  public void attachFeatureStore(FeatureStore fs) {
    this.featureStore = fs;
  }

  public java.util.Map<String, Double> getFeatures(String symbol, long ts) {
    if (featureStore == null) return java.util.Collections.emptyMap();
    return featureStore.getFeatures(symbol, ts);
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
