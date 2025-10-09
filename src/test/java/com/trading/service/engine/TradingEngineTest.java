package com.trading.service.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.trading.service.engine.impl.SimpleBroker;
import com.trading.service.engine.impl.SimpleRiskManager;
import com.trading.service.engine.impl.StdOutExecutionLogger;
import com.trading.service.model.Portfolio;
import com.trading.service.model.Signal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class TradingEngineTest {

  @Test
  public void executesBuyAndSellWithRiskApproval() {
    Portfolio p = new Portfolio(10_000);
    TradingEngine engine =
        new TradingEngine(
            p,
            new SimpleBroker(),
            new SimpleRiskManager(50_000, 10_000),
            new StdOutExecutionLogger());

    Map<String, Double> prices = new HashMap<>();
    prices.put("TEST", 100.0);

    // BUY 50 @ 100
    Signal buy = new Signal(Signal.SignalType.BUY, 0.9, "model", "1", System.currentTimeMillis());
    boolean ok =
        engine.processSignal(
            "TEST", buy, 50, 100.0, Arrays.asList(0.01, -0.005, 0.002), 0.95, prices);
    assertTrue(ok);
    assertEquals(10_000 - 50 * 100.0, p.getCash(), 1e-6);

    // Update price, SELL 50 @ 105 -> realizedPnL ~ +250
    prices.put("TEST", 105.0);
    Signal sell = new Signal(Signal.SignalType.SELL, 0.9, "model", "1", System.currentTimeMillis());
    ok =
        engine.processSignal(
            "TEST", sell, 50, 105.0, Arrays.asList(0.01, -0.005, 0.002), 0.95, prices);
    assertTrue(ok);
    assertEquals(10_000 - 50 * 100.0 + 50 * 105.0, p.getCash(), 1e-6);
    assertEquals(250.0, engine.getRealizedPnL(), 1e-6);
  }
}
