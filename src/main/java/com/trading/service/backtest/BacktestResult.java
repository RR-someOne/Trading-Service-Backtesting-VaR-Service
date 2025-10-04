package com.trading.service.backtest;

import com.trading.service.model.Portfolio;
import java.util.List;

/** Comprehensive backtest results including performance metrics and VaR analysis. */
public class BacktestResult {
  private final String status;
  private final List<Double> portfolioValues;
  private final List<Double> portfolioReturns;
  private final List<VaRReport> dailyVaRReports;
  private final Portfolio finalPortfolio;
  private final double initialValue;
  private final double finalValue;

  // Constructor for error cases
  public BacktestResult(
      String status, List<Double> portfolioValues, List<Double> portfolioReturns) {
    this.status = status;
    this.portfolioValues = portfolioValues;
    this.portfolioReturns = portfolioReturns;
    this.dailyVaRReports = List.of();
    this.finalPortfolio = null;
    this.initialValue = 0.0;
    this.finalValue = 0.0;
  }

  // Constructor for successful backtest
  public BacktestResult(
      String status,
      List<Double> portfolioValues,
      List<Double> portfolioReturns,
      List<VaRReport> dailyVaRReports,
      Portfolio finalPortfolio,
      double initialValue,
      double finalValue) {
    this.status = status;
    this.portfolioValues = portfolioValues;
    this.portfolioReturns = portfolioReturns;
    this.dailyVaRReports = dailyVaRReports;
    this.finalPortfolio = finalPortfolio;
    this.initialValue = initialValue;
    this.finalValue = finalValue;
  }

  // Getters
  public String getStatus() {
    return status;
  }

  public List<Double> getPortfolioValues() {
    return portfolioValues;
  }

  public List<Double> getPortfolioReturns() {
    return portfolioReturns;
  }

  public List<VaRReport> getDailyVaRReports() {
    return dailyVaRReports;
  }

  public Portfolio getFinalPortfolio() {
    return finalPortfolio;
  }

  public double getInitialValue() {
    return initialValue;
  }

  public double getFinalValue() {
    return finalValue;
  }

  /** Calculate total return percentage. */
  public double getTotalReturn() {
    if (initialValue <= 0) return 0.0;
    return ((finalValue - initialValue) / initialValue) * 100;
  }

  /** Calculate maximum drawdown from portfolio values. */
  public double getMaxDrawdown() {
    if (portfolioValues.isEmpty()) return 0.0;

    double maxValue = 0.0;
    double maxDrawdown = 0.0;

    for (double value : portfolioValues) {
      if (value > maxValue) {
        maxValue = value;
      }
      double drawdown = (maxValue - value) / maxValue;
      if (drawdown > maxDrawdown) {
        maxDrawdown = drawdown;
      }
    }

    return maxDrawdown * 100; // Return as percentage
  }

  /** Calculate Sharpe ratio (assuming risk-free rate of 0). */
  public double getSharpeRatio() {
    if (portfolioReturns.isEmpty()) return 0.0;

    double meanReturn = portfolioReturns.stream().mapToDouble(d -> d).average().orElse(0.0);
    double variance =
        portfolioReturns.stream()
            .mapToDouble(r -> Math.pow(r - meanReturn, 2))
            .average()
            .orElse(0.0);
    double stdDev = Math.sqrt(variance);

    if (stdDev == 0) return 0.0;

    // Annualize (assuming daily returns)
    double annualizedReturn = meanReturn * 252;
    double annualizedVolatility = stdDev * Math.sqrt(252);

    return annualizedReturn / annualizedVolatility;
  }

  /** Get average VaR across all reports. */
  public double getAverageHistoricalVaR() {
    return dailyVaRReports.stream().mapToDouble(VaRReport::getHistoricalVaR).average().orElse(0.0);
  }

  public double getAverageParametricVaR() {
    return dailyVaRReports.stream().mapToDouble(VaRReport::getParametricVaR).average().orElse(0.0);
  }

  public double getAverageMonteCarloVaR() {
    return dailyVaRReports.stream().mapToDouble(VaRReport::getMonteCarloVaR).average().orElse(0.0);
  }

  /** Generate summary report. */
  public String getSummaryReport() {
    StringBuilder sb = new StringBuilder();
    sb.append("=== BACKTEST RESULTS SUMMARY ===\n");
    sb.append(String.format("Status: %s\n", status));
    sb.append(String.format("Initial Value: $%.2f\n", initialValue));
    sb.append(String.format("Final Value: $%.2f\n", finalValue));
    sb.append(String.format("Total Return: %.2f%%\n", getTotalReturn()));
    sb.append(String.format("Max Drawdown: %.2f%%\n", getMaxDrawdown()));
    sb.append(String.format("Sharpe Ratio: %.3f\n", getSharpeRatio()));
    sb.append(String.format("Trading Days: %d\n", portfolioValues.size()));
    sb.append(String.format("VaR Reports: %d\n", dailyVaRReports.size()));

    if (!dailyVaRReports.isEmpty()) {
      sb.append("\n=== AVERAGE VaR METRICS ===\n");
      sb.append(String.format("Average Historical VaR: $%.2f\n", getAverageHistoricalVaR()));
      sb.append(String.format("Average Parametric VaR: $%.2f\n", getAverageParametricVaR()));
      sb.append(String.format("Average Monte Carlo VaR: $%.2f\n", getAverageMonteCarloVaR()));
    }

    if (finalPortfolio != null) {
      sb.append("\n=== FINAL PORTFOLIO ===\n");
      sb.append(String.format("Cash: $%.2f\n", finalPortfolio.getCash()));
      sb.append(String.format("Positions: %s\n", finalPortfolio.getPositions()));
    }

    return sb.toString();
  }

  @Override
  public String toString() {
    return String.format(
        "BacktestResult{status='%s', totalReturn=%.2f%%, maxDrawdown=%.2f%%, sharpe=%.3f}",
        status, getTotalReturn(), getMaxDrawdown(), getSharpeRatio());
  }
}
