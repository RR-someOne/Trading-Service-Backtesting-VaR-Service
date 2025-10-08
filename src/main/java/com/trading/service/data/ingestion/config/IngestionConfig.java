package com.trading.service.data.ingestion.config;

import java.time.Duration;
import java.util.Objects;

/** Central configuration for market data ingestion components. */
public class IngestionConfig {
  private final String kafkaBootstrapServers;
  private final String tickTopic;
  private final String barTopic;
  private final Duration restPollInterval;
  private final int dispatcherQueueCapacity;
  private final int archiverBatchSize;

  private IngestionConfig(Builder b) {
    this.kafkaBootstrapServers = b.kafkaBootstrapServers;
    this.tickTopic = b.tickTopic;
    this.barTopic = b.barTopic;
    this.restPollInterval = b.restPollInterval;
    this.dispatcherQueueCapacity = b.dispatcherQueueCapacity;
    this.archiverBatchSize = b.archiverBatchSize;
  }

  public String getKafkaBootstrapServers() {
    return kafkaBootstrapServers;
  }

  public String getTickTopic() {
    return tickTopic;
  }

  public String getBarTopic() {
    return barTopic;
  }

  public Duration getRestPollInterval() {
    return restPollInterval;
  }

  public int getDispatcherQueueCapacity() {
    return dispatcherQueueCapacity;
  }

  public int getArchiverBatchSize() {
    return archiverBatchSize;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private String kafkaBootstrapServers = "localhost:9092";
    private String tickTopic = "market-data-ticks";
    private String barTopic = "market-data-bars";
    private Duration restPollInterval = Duration.ofSeconds(1);
    private int dispatcherQueueCapacity = 50_000;
    private int archiverBatchSize = 10_000;

    public Builder kafkaBootstrapServers(String v) {
      this.kafkaBootstrapServers = v;
      return this;
    }

    public Builder tickTopic(String v) {
      this.tickTopic = v;
      return this;
    }

    public Builder barTopic(String v) {
      this.barTopic = v;
      return this;
    }

    public Builder restPollInterval(Duration v) {
      this.restPollInterval = v;
      return this;
    }

    public Builder dispatcherQueueCapacity(int v) {
      this.dispatcherQueueCapacity = v;
      return this;
    }

    public Builder archiverBatchSize(int v) {
      this.archiverBatchSize = v;
      return this;
    }

    public IngestionConfig build() {
      Objects.requireNonNull(kafkaBootstrapServers, "bootstrap servers");
      return new IngestionConfig(this);
    }
  }
}
