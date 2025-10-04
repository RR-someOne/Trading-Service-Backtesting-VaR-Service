package com.trading.service.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * OHLC (Open, High, Low, Close) bar data for historical price series. Represents price data for a
 * specific time period (e.g., daily, hourly).
 */
public class OHLC {
  private final String symbol;
  private final LocalDate date;
  private final double open;
  private final double high;
  private final double low;
  private final double close;
  private final long volume;
  private final LocalDateTime timestamp;

  public OHLC(
      String symbol,
      LocalDate date,
      double open,
      double high,
      double low,
      double close,
      long volume) {
    this.symbol = symbol;
    this.date = date;
    this.open = open;
    this.high = high;
    this.low = low;
    this.close = close;
    this.volume = volume;
    this.timestamp = date.atStartOfDay();
  }

  public OHLC(
      String symbol,
      LocalDateTime timestamp,
      double open,
      double high,
      double low,
      double close,
      long volume) {
    this.symbol = symbol;
    this.date = timestamp.toLocalDate();
    this.open = open;
    this.high = high;
    this.low = low;
    this.close = close;
    this.volume = volume;
    this.timestamp = timestamp;
  }

  public String getSymbol() {
    return symbol;
  }

  public LocalDate getDate() {
    return date;
  }

  public double getOpen() {
    return open;
  }

  public double getHigh() {
    return high;
  }

  public double getLow() {
    return low;
  }

  public double getClose() {
    return close;
  }

  public long getVolume() {
    return volume;
  }

  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  /**
   * Calculate daily return based on close prices.
   *
   * @param previousClose Previous day's close price
   * @return Daily return as percentage (e.g., 0.02 for 2%)
   */
  public double getDailyReturn(double previousClose) {
    if (previousClose <= 0) {
      return 0.0;
    }
    return (close - previousClose) / previousClose;
  }

  /**
   * Calculate log return for more stable calculations.
   *
   * @param previousClose Previous day's close price
   * @return Log return
   */
  public double getLogReturn(double previousClose) {
    if (previousClose <= 0 || close <= 0) {
      return 0.0;
    }
    return Math.log(close / previousClose);
  }

  @Override
  public String toString() {
    return String.format(
        "OHLC{symbol='%s', date=%s, open=%.2f, high=%.2f, low=%.2f, close=%.2f, volume=%d}",
        symbol, date, open, high, low, close, volume);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    OHLC ohlc = (OHLC) o;
    return symbol.equals(ohlc.symbol) && date.equals(ohlc.date);
  }

  @Override
  public int hashCode() {
    return symbol.hashCode() * 31 + date.hashCode();
  }
}
