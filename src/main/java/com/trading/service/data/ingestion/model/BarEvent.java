package com.trading.service.data.ingestion.model;

/** Normalized aggregated bar (OHLCV) event (POJO for compatibility). */
public class BarEvent {
  private final String symbol;
  private final String interval;
  private final long startEpochMillis;
  private final long endEpochMillis;
  private final double open;
  private final double high;
  private final double low;
  private final double close;
  private final double volume;

  public BarEvent(
      String symbol,
      String interval,
      long startEpochMillis,
      long endEpochMillis,
      double open,
      double high,
      double low,
      double close,
      double volume) {
    this.symbol = symbol;
    this.interval = interval;
    this.startEpochMillis = startEpochMillis;
    this.endEpochMillis = endEpochMillis;
    this.open = open;
    this.high = high;
    this.low = low;
    this.close = close;
    this.volume = volume;
  }

  public String symbol() {
    return symbol;
  }

  public String interval() {
    return interval;
  }

  public long startEpochMillis() {
    return startEpochMillis;
  }

  public long endEpochMillis() {
    return endEpochMillis;
  }

  public double open() {
    return open;
  }

  public double high() {
    return high;
  }

  public double low() {
    return low;
  }

  public double close() {
    return close;
  }

  public double volume() {
    return volume;
  }
}
