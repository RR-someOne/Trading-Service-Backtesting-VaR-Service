package com.trading.service.data.ingestion;

import com.trading.service.data.ingestion.archive.BatchArchiver;
import com.trading.service.data.ingestion.model.BarEvent;
import com.trading.service.data.ingestion.model.MarketDataEvent;
import com.trading.service.data.ingestion.publish.IngressDispatcher;
import com.trading.service.data.ingestion.publish.Publisher;
import com.trading.service.data.ingestion.timeseries.TimeSeriesWriter;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/** Basic test for IngressDispatcher multi-sink fan-out behavior using in-memory fakes. */
public class IngestionDispatcherTest {

  // Simple publisher stub capturing events (does not use Kafka)
  private static class RecordingPublisher implements Publisher {
    final List<MarketDataEvent> ticks = new ArrayList<>();
    final List<BarEvent> bars = new ArrayList<>();

    public void publishTick(MarketDataEvent evt) {
      ticks.add(evt);
    }

    public void publishBar(BarEvent evt) {
      bars.add(evt);
    }

    @Override
    public void close() {}
  }

  private static class RecordingWriter implements TimeSeriesWriter {
    final List<MarketDataEvent> ticks = new ArrayList<>();
    final List<BarEvent> bars = new ArrayList<>();

    @Override
    public void writeTick(MarketDataEvent e) {
      ticks.add(e);
    }

    @Override
    public void writeBar(BarEvent e) {
      bars.add(e);
    }

    @Override
    public void close() {}
  }

  private static class RecordingArchiver implements BatchArchiver {
    final List<MarketDataEvent> ticks = new ArrayList<>();
    final List<BarEvent> bars = new ArrayList<>();

    @Override
    public void acceptTick(MarketDataEvent e) {
      ticks.add(e);
    }

    @Override
    public void acceptBar(BarEvent e) {
      bars.add(e);
    }

    @Override
    public void flush() {}

    @Override
    public void close() {}
  }

  private RecordingPublisher publisher;
  private RecordingWriter writer;
  private RecordingArchiver archiver;
  private IngressDispatcher dispatcher;

  @Before
  public void setUp() {
    publisher = new RecordingPublisher();
    writer = new RecordingWriter();
    archiver = new RecordingArchiver();
    dispatcher = new IngressDispatcher(publisher, writer, archiver, 100);
    dispatcher.start();
  }

  @After
  public void tearDown() {
    dispatcher.close();
  }

  @Test
  public void testFanOutTick() throws Exception {
    MarketDataEvent evt = new MarketDataEvent("AAPL", System.currentTimeMillis(), 1, 2, 1.5, 10);
    dispatcher.submitTick(evt);
    Thread.sleep(50); // Allow worker to process
    org.junit.Assert.assertEquals(1, publisher.ticks.size());
    org.junit.Assert.assertEquals(1, writer.ticks.size());
    org.junit.Assert.assertEquals(1, archiver.ticks.size());
  }

  @Test
  public void testFanOutBar() throws Exception {
    BarEvent bar = new BarEvent("AAPL", "1m", 0, 60_000, 1, 2, 0.5, 1.5, 100);
    dispatcher.submitBar(bar);
    Thread.sleep(50);
    org.junit.Assert.assertEquals(1, publisher.bars.size());
    org.junit.Assert.assertEquals(1, writer.bars.size());
    org.junit.Assert.assertEquals(1, archiver.bars.size());
  }
}
