package com.trading.service.backtest;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Configuration class for backtesting parameters. */
public class BacktestConfig {
  private final String description;
  private final List<String> assetSymbols;
  private final Map<String, String> dataFilePaths;
  private final LocalDate startDate;
  private final LocalDate endDate;
  private final double initialCash;
  private final Object strategy; // Temporarily using Object instead of TradingStrategy
  private final int varWindowSize;
  private final double varConfidenceLevel;

  public BacktestConfig(
      String description,
      List<String> assetSymbols,
      Map<String, String> dataFilePaths,
      LocalDate startDate,
      LocalDate endDate,
      double initialCash,
      Object strategy,
      int varWindowSize,
      double varConfidenceLevel) {
    this.description = description;
    this.assetSymbols = assetSymbols;
    this.dataFilePaths = dataFilePaths != null ? dataFilePaths : new HashMap<>();
    this.startDate = startDate;
    this.endDate = endDate;
    this.initialCash = initialCash;
    this.strategy = strategy;
    this.varWindowSize = varWindowSize;
    this.varConfidenceLevel = varConfidenceLevel;
  }

  // Getters
  public String getDescription() {
    return description;
  }

  public List<String> getAssetSymbols() {
    return assetSymbols;
  }

  public Map<String, String> getDataFilePaths() {
    return dataFilePaths;
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public LocalDate getEndDate() {
    return endDate;
  }

  public double getInitialCash() {
    return initialCash;
  }

  public Object getStrategy() {
    return strategy;
  }

  public int getVarWindowSize() {
    return varWindowSize;
  }

  public double getVarConfidenceLevel() {
    return varConfidenceLevel;
  }

  @Override
  public String toString() {
    return String.format(
        "BacktestConfig{description='%s', assets=%s, period=%s to %s, cash=%.2f}",
        description, assetSymbols, startDate, endDate, initialCash);
  }
}
