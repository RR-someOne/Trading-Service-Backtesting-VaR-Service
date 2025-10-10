package com.trading.service;

import com.trading.service.api.ControlHandler;
import com.trading.service.api.RestApiServer;
import com.trading.service.data.ingestion.archive.LocalFileBatchArchiver;
import com.trading.service.data.ingestion.config.IngestionConfig;
import com.trading.service.data.ingestion.connector.MarketDataConnector;
import com.trading.service.data.ingestion.connector.RestPollingMarketDataConnector;
import com.trading.service.data.ingestion.gateway.IngestionGateway;
import com.trading.service.data.ingestion.publish.MarketDataPublisher;
import com.trading.service.data.ingestion.publish.NoOpPublisher;
import com.trading.service.data.ingestion.publish.Publisher;
import com.trading.service.data.ingestion.service.MarketDataIngestionService;
import com.trading.service.data.ingestion.timeseries.FileCsvTimeSeriesWriter;
import com.trading.service.persistence.featurestore.CsvTimeSeriesRepository;
import com.trading.service.persistence.featurestore.InHouseFeatureStore;
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
    boolean kafkaEnabled =
        Boolean.parseBoolean(System.getenv().getOrDefault("KAFKA_ENABLED", "false"));
    Publisher publisher = kafkaEnabled ? new MarketDataPublisher(cfg) : new NoOpPublisher();
    if (!kafkaEnabled) {
      System.out.println(
          "KAFKA_ENABLED is false. Using NoOpPublisher; events won't be sent to Kafka.");
    }
    FileCsvTimeSeriesWriter writer = new FileCsvTimeSeriesWriter(Path.of("data-output"));
    LocalFileBatchArchiver archiver =
        new LocalFileBatchArchiver(cfg, Path.of("data-output/archive"));
    MarketDataIngestionService service =
        new MarketDataIngestionService(cfg, publisher, writer, archiver);

    // Feature Store: durable CSV-backed time-series + deterministic rolling features
    CsvTimeSeriesRepository featureRepo = new CsvTimeSeriesRepository(Path.of("feature-data"));
    InHouseFeatureStore featureStore = new InHouseFeatureStore(featureRepo, 20, 252.0);
    service.attachFeatureStore(featureStore);

    MarketDataConnector rest =
        new RestPollingMarketDataConnector(
            URI.create("https://example.com/marketdata"), Duration.ofSeconds(5), sched);
    service.registerConnector(rest);

    // Attach gateway for raw message parsing
    IngestionGateway gateway = new IngestionGateway(service);
    service.attachGateway(gateway);
    service.startAll();

    // Optional REST API for status/control
    boolean enableApi = Boolean.parseBoolean(System.getenv().getOrDefault("ENABLE_API", "true"));
    RestApiServer restServer = null;
    if (enableApi) {
      String apiKey = System.getenv().getOrDefault("API_KEY", "");
      int restPort = Integer.parseInt(System.getenv().getOrDefault("API_REST_PORT", "8080"));
      ControlHandler handler =
          new ControlHandler() {
            @Override
            public void startIngestion() {
              service.startAll();
            }

            @Override
            public void stopIngestion() {
              service.stopAll();
            }

            @Override
            public java.util.Map<String, Object> status() {
              return java.util.Map.of(
                  "ingestionRunning", service.isRunning(),
                  "parsed", gateway.getParsedCount(),
                  "dropped", gateway.getDroppedCount());
            }
          };
      restServer = new RestApiServer(restPort, apiKey, handler);
    }

    final RestApiServer restServerRef = restServer;
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  System.out.println("Shutting down ingestion...");
                  try {
                    service.close();
                  } catch (Exception ignored) {
                  }
                  try {
                    if (restServerRef != null) restServerRef.close();
                  } catch (Exception ignored) {
                  }
                  try {
                    featureRepo.close();
                  } catch (Exception ignored) {
                  }
                  sched.shutdownNow();
                }));

    System.out.println(
        "Ingestion service started with gateway. Parsed="
            + gateway.getParsedCount()
            + ", dropped="
            + gateway.getDroppedCount()
            + ". Ctrl+C to exit.");
  }
}
