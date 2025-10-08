package com.trading.service.data.ingestion.model;

/** Normalized tick-level event (POJO instead of record for broader compatibility). */
public class MarketDataEvent {
  private final String symbol;
  private final long timestampEpochMillis;
  private final double bid;
  private final double ask;
  private final double last;
  private final double volume;

  public MarketDataEvent(
      String symbol,
      long timestampEpochMillis,
      double bid,
      double ask,
      double last,
      double volume) {
    this.symbol = symbol;
    this.timestampEpochMillis = timestampEpochMillis;
    this.bid = bid;
    this.ask = ask;
    this.last = last;
    this.volume = volume;
  }

  public String symbol() {
    return symbol;
  }

  public long timestampEpochMillis() {
    return timestampEpochMillis;
  }

  public double bid() {
    return bid;
  }

  public double ask() {
    return ask;
  }

  public double last() {
    return last;
  }

  public double volume() {
    return volume;
  }
}
