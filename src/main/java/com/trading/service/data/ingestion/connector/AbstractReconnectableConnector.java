package com.trading.service.data.ingestion.connector;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/** Lightweight base providing simple reconnect loop scaffolding. */
public abstract class AbstractReconnectableConnector implements MarketDataConnector {
  private final ScheduledExecutorService scheduler;
  private final Duration reconnectDelay;
  private final AtomicBoolean running = new AtomicBoolean();
  private volatile ScheduledFuture<?> future;

  protected AbstractReconnectableConnector(
      ScheduledExecutorService scheduler, Duration reconnectDelay) {
    this.scheduler = scheduler;
    this.reconnectDelay = reconnectDelay;
  }

  protected abstract boolean doConnect();

  protected abstract void doDisconnect();

  protected abstract String connectorName();

  @Override
  public void start() {
    if (running.compareAndSet(false, true)) scheduleConnect();
  }

  private void scheduleConnect() {
    future = scheduler.schedule(this::attemptConnect, 0, TimeUnit.MILLISECONDS);
  }

  private void attemptConnect() {
    if (!running.get()) return;
    boolean ok = false;
    try {
      ok = doConnect();
    } catch (Exception e) {
      ok = false;
    }
    if (!ok && running.get()) {
      // reschedule
      future =
          scheduler.schedule(
              this::attemptConnect, reconnectDelay.toMillis(), TimeUnit.MILLISECONDS);
    }
  }

  @Override
  public boolean isRunning() {
    return running.get();
  }

  @Override
  public void close() {
    running.set(false);
    if (future != null) future.cancel(true);
    try {
      doDisconnect();
    } catch (Exception ignored) {
    }
  }

  @Override
  public String name() {
    return connectorName();
  }
}
