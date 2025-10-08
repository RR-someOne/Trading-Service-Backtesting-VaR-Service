package com.trading.service;

import com.trading.service.data.ingestion.archive.LocalFileBatchArchiver;
import com.trading.service.data.ingestion.config.IngestionConfig;
import com.trading.service.data.ingestion.connector.MarketDataConnector;
import com.trading.service.data.ingestion.connector.RestPollingMarketDataConnector;
import com.trading.service.data.ingestion.publish.MarketDataPublisher;
import com.trading.service.data.ingestion.service.MarketDataIngestionService;
import com.trading.service.data.ingestion.timeseries.FileCsvTimeSeriesWriter;
import java.net.URI;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public final class Application {
  private Application() {}

  public static void main(String[] args) throws Exception {
    System.out.println("Trading Service with AI, Backtesting, VaR & Ingestion");
    boolean enableIngestion =
        Boolean.parseBoolean(System.getenv().getOrDefault("ENABLE_INGESTION", "false"));
    if (!enableIngestion) {
      System.out.println("Ingestion disabled (set ENABLE_INGESTION=true to enable).");
      return;
    }

    IngestionConfig cfg = IngestionConfig.builder().build();
    ScheduledExecutorService sched = Executors.newScheduledThreadPool(2);
    MarketDataPublisher publisher = new MarketDataPublisher(cfg);
    FileCsvTimeSeriesWriter writer = new FileCsvTimeSeriesWriter(Path.of("data-output"));
    LocalFileBatchArchiver archiver =
        new LocalFileBatchArchiver(cfg, Path.of("data-output/archive"));
    MarketDataIngestionService service =
        new MarketDataIngestionService(cfg, publisher, writer, archiver);

    MarketDataConnector rest =
        new RestPollingMarketDataConnector(
            URI.create("https://example.com/marketdata"), Duration.ofSeconds(5), sched);
    service.registerConnector(rest);
    service.startAll();

    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  System.out.println("Shutting down ingestion...");
                  try {
                    service.close();
                  } catch (Exception ignored) {
                  }
                  sched.shutdownNow();
                }));

    System.out.println("Ingestion service started. Press Ctrl+C to exit.");
  }
}
