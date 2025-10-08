package com.trading.service.data.ingestion.connector;

import com.trading.service.data.ingestion.model.BarEvent;
import com.trading.service.data.ingestion.model.MarketDataEvent;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/** Simple REST polling connector (placeholder fetch + parse). */
@SuppressWarnings("unused")
public class RestPollingMarketDataConnector implements MarketDataConnector {
  private final URI endpoint;
  private final Duration interval;
  private final ScheduledExecutorService scheduler;
  private final HttpClient client = HttpClient.newHttpClient();
  private final AtomicBoolean running = new AtomicBoolean();
  private volatile Consumer<MarketDataEvent> tickHandler = e -> {};
  private volatile Consumer<BarEvent> barHandler = e -> {};

  public RestPollingMarketDataConnector(
      URI endpoint, Duration interval, ScheduledExecutorService scheduler) {
    this.endpoint = endpoint;
    this.interval = interval;
    this.scheduler = scheduler;
  }

  @Override
  public void start() {
    if (running.compareAndSet(false, true))
      scheduler.scheduleAtFixedRate(this::poll, 0, interval.toMillis(), TimeUnit.MILLISECONDS);
  }

  private void poll() {
    if (!running.get()) return;
    try {
      HttpRequest req = HttpRequest.newBuilder(endpoint).GET().build();
      client
          .sendAsync(req, HttpResponse.BodyHandlers.ofString())
          .thenAccept(
              r -> {
                // TODO parse JSON -> events; placeholder dispatch
                tickHandler.accept(
                    new MarketDataEvent("DEMO", System.currentTimeMillis(), 0, 0, 0, 0));
              });
    } catch (Exception ignored) {
    }
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
  public void close() {
    running.set(false);
  } // rely on flag to stop scheduling

  @Override
  public String name() {
    return "rest:" + endpoint;
  }
}
