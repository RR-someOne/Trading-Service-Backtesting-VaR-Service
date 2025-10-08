package com.trading.service.data.ingestion.gateway;

import static org.junit.Assert.assertEquals;

import com.trading.service.data.ingestion.archive.BatchArchiver;
import com.trading.service.data.ingestion.archive.LocalFileBatchArchiver;
import com.trading.service.data.ingestion.config.IngestionConfig;
import com.trading.service.data.ingestion.model.BarEvent;
import com.trading.service.data.ingestion.model.MarketDataEvent;
import com.trading.service.data.ingestion.publish.Publisher;
import com.trading.service.data.ingestion.service.MarketDataIngestionService;
import com.trading.service.data.ingestion.timeseries.FileCsvTimeSeriesWriter;
import com.trading.service.data.ingestion.timeseries.TimeSeriesWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

@SuppressWarnings("BusyWait")
public class IngestionGatewayTest {

  private static class CapturingPublisher implements Publisher {
    final List<MarketDataEvent> ticks = new ArrayList<>();
    final List<BarEvent> bars = new ArrayList<>();

    @Override
    public void publishTick(MarketDataEvent event) {
      ticks.add(event);
    }

    @Override
    public void publishBar(BarEvent bar) {
      bars.add(bar);
    }

    @Override
    public void close() {}
  }

  @Test
  public void parsesTick() {
    IngestionConfig cfg = IngestionConfig.builder().build();
    CapturingPublisher pub = new CapturingPublisher();
    TimeSeriesWriter writer = new FileCsvTimeSeriesWriter(Paths.get("build/test-gateway"));
    BatchArchiver archiver =
        new LocalFileBatchArchiver(cfg, Paths.get("build/test-gateway/archive"));
    MarketDataIngestionService svc = new MarketDataIngestionService(cfg, pub, writer, archiver);
    IngestionGateway gw = new IngestionGateway(svc);
    svc.attachGateway(gw);

    String raw =
        "{\"symbol\":\"TEST\",\"bid\":100.1,\"ask\":100.2,\"last\":100.15,\"volume\":10,\"ts\":123456789}";
    gw.onRaw(raw);
    await(() -> pub.ticks.size() == 1, 1000);
    assertEquals(1, pub.ticks.size());
    MarketDataEvent e = pub.ticks.get(0);
    assertEquals("TEST", e.symbol());
  }

  @Test
  public void parsesBar() {
    IngestionConfig cfg = IngestionConfig.builder().build();
    CapturingPublisher pub = new CapturingPublisher();
    TimeSeriesWriter writer = new FileCsvTimeSeriesWriter(Paths.get("build/test-gateway"));
    BatchArchiver archiver =
        new LocalFileBatchArchiver(cfg, Paths.get("build/test-gateway/archive"));
    MarketDataIngestionService svc = new MarketDataIngestionService(cfg, pub, writer, archiver);
    IngestionGateway gw = new IngestionGateway(svc);
    svc.attachGateway(gw);

    String raw =
        "{\"symbol\":\"TEST\",\"open\":1,\"high\":2,\"low\":0.5,\"close\":1.5,\"volume\":100,\"start\":1000,\"end\":1600,\"interval\":\"1m\"}";
    gw.onRaw(raw);
    await(() -> pub.bars.size() == 1, 1000);
    assertEquals(1, pub.bars.size());
    BarEvent b = pub.bars.get(0);
    assertEquals("TEST", b.symbol());
  }

  private void await(Check c, long timeoutMs) {
    long deadline = System.currentTimeMillis() + timeoutMs;
    while (System.currentTimeMillis() < deadline) {
      if (c.ok()) return;
      try {
        Thread.sleep(10);
      } catch (InterruptedException ignored) {
      }
    }
  }

  private interface Check {
    boolean ok();
  }
}
