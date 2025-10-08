package com.trading.service.data.ingestion.publish;

import com.trading.service.data.ingestion.archive.BatchArchiver;
import com.trading.service.data.ingestion.model.BarEvent;
import com.trading.service.data.ingestion.model.MarketDataEvent;
import com.trading.service.data.ingestion.timeseries.TimeSeriesWriter;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/** Multi-sink dispatcher: Kafka + TimeSeries + Archiver. */
public class IngressDispatcher implements AutoCloseable {
  private final Publisher publisher;
  private final TimeSeriesWriter timeSeriesWriter;
  private final BatchArchiver archiver;
  private final BlockingQueue<Object> queue;
  private final Thread worker;
  private volatile boolean running = true;

  public IngressDispatcher(
      Publisher publisher, TimeSeriesWriter writer, BatchArchiver archiver, int capacity) {
    this.publisher = publisher;
    this.timeSeriesWriter = writer;
    this.archiver = archiver;
    this.queue = new ArrayBlockingQueue<>(capacity);
    this.worker = new Thread(this::runLoop, "ingress-dispatcher");
  }

  public void start() {
    if (!worker.isAlive()) worker.start();
  }

  public void submitTick(MarketDataEvent e) {
    queue.offer(e);
  }

  public void submitBar(BarEvent e) {
    queue.offer(e);
  }

  private void runLoop() {
    while (running || !queue.isEmpty()) {
      try {
        Object o = queue.poll();
        if (o == null) {
          continue;
        }
        if (o instanceof MarketDataEvent) {
          MarketDataEvent m = (MarketDataEvent) o;
          publisher.publishTick(m);
          timeSeriesWriter.writeTick(m);
          archiver.acceptTick(m);
        } else if (o instanceof BarEvent) {
          BarEvent b = (BarEvent) o;
          publisher.publishBar(b);
          timeSeriesWriter.writeBar(b);
          archiver.acceptBar(b);
        }
      } catch (Exception ignored) {
      }
    }
  }

  @Override
  public void close() {
    running = false;
    try {
      worker.join(1000);
    } catch (InterruptedException ignored) {
    }
    try {
      archiver.close();
    } catch (Exception ignored) {
    }
    try {
      timeSeriesWriter.close();
    } catch (Exception ignored) {
    }
    try {
      publisher.close();
    } catch (Exception ignored) {
    }
  }
}
