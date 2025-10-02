package com.trading.service.model;

import static org.junit.Assert.*;

import org.junit.Test;

/** Unit tests for Signal class. */
public class SignalTest {

  @Test
  public void testSignalTypeFromValue() {
    assertEquals(Signal.SignalType.BUY, Signal.SignalType.fromValue(1));
    assertEquals(Signal.SignalType.SELL, Signal.SignalType.fromValue(-1));
    assertEquals(Signal.SignalType.HOLD, Signal.SignalType.fromValue(0));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSignalTypeFromInvalidValue() {
    Signal.SignalType.fromValue(99);
  }

  @Test
  public void testSignalTypeFromString() {
    assertEquals(Signal.SignalType.BUY, Signal.SignalType.fromString("BUY"));
    assertEquals(Signal.SignalType.BUY, Signal.SignalType.fromString("buy"));
    assertEquals(Signal.SignalType.SELL, Signal.SignalType.fromString("SELL"));
    assertEquals(Signal.SignalType.SELL, Signal.SignalType.fromString("sell"));
    assertEquals(Signal.SignalType.HOLD, Signal.SignalType.fromString("HOLD"));
    assertEquals(Signal.SignalType.HOLD, Signal.SignalType.fromString("hold"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSignalTypeFromNullString() {
    Signal.SignalType.fromString(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSignalTypeFromInvalidString() {
    Signal.SignalType.fromString("INVALID");
  }

  @Test
  public void testSignalTypeGetValue() {
    assertEquals(1, Signal.SignalType.BUY.getValue());
    assertEquals(-1, Signal.SignalType.SELL.getValue());
    assertEquals(0, Signal.SignalType.HOLD.getValue());
  }

  @Test
  public void testSignalCreation() {
    long timestamp = System.currentTimeMillis();
    Signal signal = new Signal(Signal.SignalType.BUY, 0.85, "rf_signal", "v1", timestamp);

    assertEquals(Signal.SignalType.BUY, signal.getType());
    assertEquals(0.85, signal.getConfidence(), 0.001);
    assertEquals("rf_signal", signal.getModelName());
    assertEquals("v1", signal.getModelVersion());
    assertEquals(timestamp, signal.getTimestamp());
  }

  @Test
  public void testSignalIsActionable() {
    // High confidence BUY signal should be actionable
    Signal buySignal =
        new Signal(Signal.SignalType.BUY, 0.8, "model", "v1", System.currentTimeMillis());
    assertTrue("High confidence BUY signal should be actionable", buySignal.isActionable());

    // High confidence SELL signal should be actionable
    Signal sellSignal =
        new Signal(Signal.SignalType.SELL, 0.7, "model", "v1", System.currentTimeMillis());
    assertTrue("High confidence SELL signal should be actionable", sellSignal.isActionable());

    // HOLD signal should not be actionable regardless of confidence
    Signal holdSignal =
        new Signal(Signal.SignalType.HOLD, 0.9, "model", "v1", System.currentTimeMillis());
    assertFalse("HOLD signal should not be actionable", holdSignal.isActionable());

    // Low confidence BUY signal should not be actionable
    Signal lowConfidenceBuy =
        new Signal(Signal.SignalType.BUY, 0.3, "model", "v1", System.currentTimeMillis());
    assertFalse(
        "Low confidence BUY signal should not be actionable", lowConfidenceBuy.isActionable());

    // Low confidence SELL signal should not be actionable
    Signal lowConfidenceSell =
        new Signal(Signal.SignalType.SELL, 0.4, "model", "v1", System.currentTimeMillis());
    assertFalse(
        "Low confidence SELL signal should not be actionable", lowConfidenceSell.isActionable());
  }

  @Test
  public void testSignalToString() {
    long timestamp = 1696176000000L; // Fixed timestamp for testing
    Signal signal = new Signal(Signal.SignalType.BUY, 0.75, "rf_signal", "v1", timestamp);

    String signalString = signal.toString();
    assertNotNull("toString should not return null", signalString);
    assertTrue("toString should contain signal type", signalString.contains("BUY"));
    assertTrue("toString should contain confidence", signalString.contains("0.75"));
    assertTrue("toString should contain model name", signalString.contains("rf_signal"));
    assertTrue("toString should contain model version", signalString.contains("v1"));
    assertTrue(
        "toString should contain timestamp", signalString.contains(String.valueOf(timestamp)));
  }

  @Test
  public void testSignalBoundaryConfidenceValues() {
    // Test boundary confidence values for actionable check
    Signal exactlyActionable =
        new Signal(Signal.SignalType.BUY, 0.5000001, "model", "v1", System.currentTimeMillis());
    assertTrue(
        "Signal with confidence just above 0.5 should be actionable",
        exactlyActionable.isActionable());

    Signal exactlyNotActionable =
        new Signal(Signal.SignalType.BUY, 0.5, "model", "v1", System.currentTimeMillis());
    assertFalse(
        "Signal with confidence exactly 0.5 should not be actionable",
        exactlyNotActionable.isActionable());

    Signal belowThreshold =
        new Signal(Signal.SignalType.SELL, 0.4999999, "model", "v1", System.currentTimeMillis());
    assertFalse(
        "Signal with confidence just below 0.5 should not be actionable",
        belowThreshold.isActionable());
  }
}
