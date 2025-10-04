package com.trading.service.backtest;

import java.time.LocalDate;

/** Daily VaR report containing various VaR calculations. */
public class VaRReport {
  private final LocalDate date;
  private final double portfolioValue;
  private final double historicalVaR;
  private final double parametricVaR;
  private final double monteCarloVaR;
  private final double confidenceLevel;
  private final int windowSize;

  public VaRReport(
      LocalDate date,
      double portfolioValue,
      double historicalVaR,
      double parametricVaR,
      double monteCarloVaR,
      double confidenceLevel,
      int windowSize) {
    this.date = date;
    this.portfolioValue = portfolioValue;
    this.historicalVaR = historicalVaR;
    this.parametricVaR = parametricVaR;
    this.monteCarloVaR = monteCarloVaR;
    this.confidenceLevel = confidenceLevel;
    this.windowSize = windowSize;
  }

  // Getters
  public LocalDate getDate() {
    return date;
  }

  public double getPortfolioValue() {
    return portfolioValue;
  }

  public double getHistoricalVaR() {
    return historicalVaR;
  }

  public double getParametricVaR() {
    return parametricVaR;
  }

  public double getMonteCarloVaR() {
    return monteCarloVaR;
  }

  public double getConfidenceLevel() {
    return confidenceLevel;
  }

  public int getWindowSize() {
    return windowSize;
  }

  /** Get VaR as percentage of portfolio value. */
  public double getHistoricalVaRPercent() {
    return portfolioValue > 0 ? (historicalVaR / portfolioValue) * 100 : 0;
  }

  public double getParametricVaRPercent() {
    return portfolioValue > 0 ? (parametricVaR / portfolioValue) * 100 : 0;
  }

  public double getMonteCarloVaRPercent() {
    return portfolioValue > 0 ? (monteCarloVaR / portfolioValue) * 100 : 0;
  }

  @Override
  public String toString() {
    return String.format(
        "VaRReport{date=%s, portfolioValue=%.2f, "
            + "historicalVaR=%.2f (%.2f%%), parametricVaR=%.2f (%.2f%%), "
            + "monteCarloVaR=%.2f (%.2f%%), confidence=%.1f%%, window=%d}",
        date,
        portfolioValue,
        historicalVaR,
        getHistoricalVaRPercent(),
        parametricVaR,
        getParametricVaRPercent(),
        monteCarloVaR,
        getMonteCarloVaRPercent(),
        confidenceLevel * 100,
        windowSize);
  }
}
