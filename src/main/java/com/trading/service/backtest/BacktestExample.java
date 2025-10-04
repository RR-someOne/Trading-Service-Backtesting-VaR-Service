package com.trading.service.backtest;

import com.trading.service.backtesting.MarketDataLoader;
import com.trading.service.model.OHLC;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Example demonstrating the backtesting system with sample data. This class shows how to: 1. Load
 * historical price series for assets A, B 2. Calculate daily returns 3. Set up backtesting
 * configuration 4. Run backtest with VaR calculations
 */
public class BacktestExample {

  public static void main(String[] args) {
    System.out.println("=== BACKTESTING EXAMPLE ===");

    // Create market data loader
    MarketDataLoader dataLoader = new MarketDataLoader();

    // Generate sample data for assets A and B
    LocalDate startDate = LocalDate.of(2023, 1, 1);
    LocalDate endDate = LocalDate.of(2023, 12, 31);
    int tradingDays = 252; // Approximate trading days in a year

    System.out.println("Generating sample historical data...");

    // Generate sample data for Asset A (starting at $100)
    List<OHLC> assetAData = dataLoader.generateSampleData("ASSET_A", startDate, tradingDays, 100.0);

    // Generate sample data for Asset B (starting at $150)
    List<OHLC> assetBData = dataLoader.generateSampleData("ASSET_B", startDate, tradingDays, 150.0);

    System.out.println("Asset A: Generated " + assetAData.size() + " records");
    System.out.println("Asset B: Generated " + assetBData.size() + " records");

    // Calculate daily returns for both assets
    List<Double> returnsA = dataLoader.calculateDailyReturns(assetAData);
    List<Double> returnsB = dataLoader.calculateDailyReturns(assetBData);

    System.out.println("Asset A daily returns calculated: " + returnsA.size() + " values");
    System.out.println("Asset B daily returns calculated: " + returnsB.size() + " values");

    // Display sample returns
    displaySampleReturns("Asset A", returnsA, assetAData);
    displaySampleReturns("Asset B", returnsB, assetBData);

    // Create backtest configuration
    BacktestConfig config =
        new BacktestConfig(
            "Sample Multi-Asset Backtest with VaR Analysis",
            Arrays.asList("ASSET_A", "ASSET_B"),
            new HashMap<>(), // No CSV files, using generated data
            startDate,
            endDate,
            100000.0, // $100,000 initial cash
            null, // No strategy for this example
            30, // 30-day VaR window
            0.95 // 95% confidence level
            );

    System.out.println("\n=== BACKTEST CONFIGURATION ===");
    System.out.println(config);

    // For this example, let's demonstrate VaR calculations on sample portfolio returns
    demonstrateVaRCalculations(returnsA, returnsB);

    System.out.println("\n=== EXAMPLE COMPLETED ===");
    System.out.println("This example demonstrates the core backtesting infrastructure.");
    System.out.println("To run a full backtest with strategies:");
    System.out.println("1. Implement a TradingStrategy");
    System.out.println("2. Create BacktestConfig with the strategy");
    System.out.println("3. Call backtest.run(config)");
  }

  private static void displaySampleReturns(
      String assetName, List<Double> returns, List<OHLC> ohlcData) {
    System.out.println("\n" + assetName + " Sample Data:");

    // Show first 5 days
    for (int i = 1; i <= Math.min(5, returns.size()); i++) {
      OHLC ohlc = ohlcData.get(i);
      double dailyReturn = returns.get(i);

      System.out.printf(
          "  %s: Close=%.2f, Return=%.4f (%.2f%%)%n",
          ohlc.getDate(), ohlc.getClose(), dailyReturn, dailyReturn * 100);
    }

    // Show statistics
    double avgReturn = returns.stream().skip(1).mapToDouble(d -> d).average().orElse(0.0);
    double volatility = calculateVolatility(returns);

    System.out.printf("  Average Daily Return: %.4f (%.2f%%)%n", avgReturn, avgReturn * 100);
    System.out.printf("  Daily Volatility: %.4f (%.2f%%)%n", volatility, volatility * 100);
    System.out.printf(
        "  Annualized Return: %.2f%%  Annualized Volatility: %.2f%%%n",
        avgReturn * 252 * 100, volatility * Math.sqrt(252) * 100);
  }

  private static double calculateVolatility(List<Double> returns) {
    if (returns.size() <= 1) return 0.0;

    double mean = returns.stream().skip(1).mapToDouble(d -> d).average().orElse(0.0);
    double variance =
        returns.stream().skip(1).mapToDouble(r -> Math.pow(r - mean, 2)).average().orElse(0.0);

    return Math.sqrt(variance);
  }

  private static void demonstrateVaRCalculations(List<Double> returnsA, List<Double> returnsB) {
    System.out.println("\n=== VAR CALCULATION DEMONSTRATION ===");

    // Simulate a simple portfolio: 50% Asset A, 50% Asset B
    double portfolioValue = 100000.0; // $100,000 portfolio

    // Calculate portfolio returns (simplified equal weighting)
    List<Double> portfolioReturns = new java.util.ArrayList<>();

    for (int i = 1; i < Math.min(returnsA.size(), returnsB.size()); i++) {
      double portfolioReturn = 0.5 * returnsA.get(i) + 0.5 * returnsB.get(i);
      portfolioReturns.add(portfolioReturn);
    }

    System.out.println("Portfolio returns calculated: " + portfolioReturns.size() + " values");

    // Calculate VaR using different methods (using last 30 days)
    int varWindow = Math.min(30, portfolioReturns.size());
    List<Double> recentReturns =
        portfolioReturns.subList(portfolioReturns.size() - varWindow, portfolioReturns.size());

    double confidenceLevel = 0.95;

    // Use VaRService methods
    double historicalVaR =
        com.trading.service.risk.VaRService.historicalVaR(
            recentReturns, confidenceLevel, portfolioValue);
    double parametricVaR =
        com.trading.service.risk.VaRService.parametricVaR(
            recentReturns, confidenceLevel, portfolioValue);
    double monteCarloVaR =
        com.trading.service.risk.VaRService.monteCarloVaR(
            recentReturns, confidenceLevel, portfolioValue, 10000);

    System.out.println("\nVaR Analysis (95% Confidence, " + varWindow + "-day window):");
    System.out.printf("  Portfolio Value: $%.2f%n", portfolioValue);
    System.out.printf(
        "  Historical VaR: $%.2f (%.2f%%)%n",
        historicalVaR, (historicalVaR / portfolioValue) * 100);
    System.out.printf(
        "  Parametric VaR: $%.2f (%.2f%%)%n",
        parametricVaR, (parametricVaR / portfolioValue) * 100);
    System.out.printf(
        "  Monte Carlo VaR: $%.2f (%.2f%%)%n",
        monteCarloVaR, (monteCarloVaR / portfolioValue) * 100);

    // Create a sample VaR report
    VaRReport varReport =
        new VaRReport(
            LocalDate.now(),
            portfolioValue,
            historicalVaR,
            parametricVaR,
            monteCarloVaR,
            confidenceLevel,
            varWindow);

    System.out.println("\nSample VaR Report:");
    System.out.println(varReport);
  }
}
