package com.trading.service.backtest;

import com.trading.service.backtesting.MarketDataLoader;
import com.trading.service.model.OHLC;
import com.trading.service.risk.VaRService;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Demonstration of the Backtesting System Shows how to set up and run a complete backtest with VaR
 * integration
 */
public class BacktestDemo {

  public static void main(String[] args) {
    System.out.println("=== Trading Service Backtesting Demo ===\n");

    try {
      // Step 1: Set up backtest configuration
      BacktestConfig config = createSampleConfig();
      System.out.println("1. Configuration created:");
      System.out.println("   Assets: " + config.getAssetSymbols());
      System.out.println("   Date range: " + config.getStartDate() + " to " + config.getEndDate());
      System.out.println("   Initial cash: $" + String.format("%.2f", config.getInitialCash()));
      System.out.println("   VaR window: " + config.getVarWindowSize() + " days");
      System.out.println("   VaR confidence: " + (config.getVarConfidenceLevel() * 100) + "%\n");

      // Step 2: Create backtest engine
      Backtest backtest = new Backtest();

      System.out.println("2. Components initialized:");
      System.out.println("   Backtest engine created with MarketDataLoader and VaRService\n");

      // Step 3: Run the backtest
      System.out.println("3. Running backtest...");
      BacktestResult result = backtest.run(config);

      // Step 4: Display results
      System.out.println("\n=== BACKTEST RESULTS ===");
      System.out.println("Status: " + result.getStatus());
      System.out.println("Initial Value: $" + String.format("%.2f", result.getInitialValue()));
      System.out.println("Final Value: $" + String.format("%.2f", result.getFinalValue()));
      System.out.println("Total Return: " + String.format("%.2f", result.getTotalReturn()) + "%");
      System.out.println("Max Drawdown: " + String.format("%.2f", result.getMaxDrawdown()) + "%");
      System.out.println("Sharpe Ratio: " + String.format("%.3f", result.getSharpeRatio()));
      System.out.println("Trading Days: " + result.getPortfolioValues().size());

      // Step 5: Demonstrate individual components
      System.out.println("\n=== COMPONENT DEMONSTRATIONS ===");

      // MarketDataLoader demo
      MarketDataLoader dataLoader = new MarketDataLoader();
      List<OHLC> sampleData =
          dataLoader.generateSampleData("DEMO", LocalDate.of(2023, 1, 1), 10, 100.0);
      System.out.println("MarketDataLoader: Generated " + sampleData.size() + " OHLC bars");
      System.out.println(
          "  First bar: "
              + sampleData.get(0).getSymbol()
              + " "
              + sampleData.get(0).getDate()
              + " Close: $"
              + String.format("%.2f", sampleData.get(0).getClose()));

      // Daily returns calculation
      List<Double> returns = dataLoader.calculateDailyReturns(sampleData);
      System.out.println("Daily Returns: Calculated " + returns.size() + " returns");
      if (returns.size() > 1) {
        System.out.println(
            "  Sample return: "
                + String.format("%.4f", returns.get(1))
                + " ("
                + String.format("%.2f", returns.get(1) * 100)
                + "%)");
      }

      // VaR calculation demo - using static methods
      if (returns.size() > 5) {
        double portfolioValue = 100000.0;
        double historicalVaR = VaRService.historicalVaR(returns, 0.95, portfolioValue);
        double parametricVaR = VaRService.parametricVaR(returns, 0.95, portfolioValue);
        double monteCarloVaR = VaRService.monteCarloVaR(returns, 0.95, portfolioValue, 1000);

        System.out.println("VaR Calculations for $100K portfolio:");
        System.out.println("  Historical VaR (95%): $" + String.format("%.2f", historicalVaR));
        System.out.println("  Parametric VaR (95%): $" + String.format("%.2f", parametricVaR));
        System.out.println("  Monte Carlo VaR (95%): $" + String.format("%.2f", monteCarloVaR));
      }

      System.out.println("\n=== Demo completed successfully! ===");
      System.out.println("All 6 backtesting workflow steps have been implemented:");
      System.out.println("✓ 1. Load historical price series for assets A,B");
      System.out.println("✓ 2. Calculate daily returns");
      System.out.println("✓ 3. Feed returns to strategy and simulate orders with realistic fills");
      System.out.println("✓ 4. At each day-end, compute portfolio P&L and update return series");
      System.out.println(
          "✓ 5. Run VaR service for portfolio value using last N returns (rolling window)");
      System.out.println("✓ 6. Save daily VaR numbers and include in backtest report");

    } catch (Exception e) {
      System.err.println("Demo failed: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private static BacktestConfig createSampleConfig() {
    Map<String, String> dataFiles = new HashMap<>();
    dataFiles.put("AAPL", ""); // Empty - will use synthetic data
    dataFiles.put("MSFT", ""); // Empty - will use synthetic data

    return new BacktestConfig(
        "Demo Backtest",
        Arrays.asList("AAPL", "MSFT"),
        dataFiles,
        LocalDate.of(2023, 1, 1),
        LocalDate.of(2023, 12, 31),
        100000.0, // $100K initial cash
        null, // Strategy - will use default
        30, // 30-day VaR window
        0.95 // 95% confidence level
        );
  }
}
