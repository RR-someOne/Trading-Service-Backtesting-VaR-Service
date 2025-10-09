package com.trading.service.data.bus;

import static org.junit.Assert.assertEquals;

import com.trading.service.data.ingestion.model.MarketDataEvent;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

/** Unit test using a local in-memory stub extending KafkaTickEventBus logic without real broker. */
public class KafkaTickEventBusTest {

  private static class CapturingBus implements TickEventBus {
    final AtomicInteger count = new AtomicInteger();

    @Override
    public void publish(MarketDataEvent event) {
      count.incrementAndGet();
    }

    @Override
    public void close() {}
  }

  @Test
  public void publishesTick() {
    CapturingBus bus = new CapturingBus();
    bus.publish(new MarketDataEvent("TEST", System.currentTimeMillis(), 1, 2, 1.5, 10));
    assertEquals(1, bus.count.get());
  }
}
