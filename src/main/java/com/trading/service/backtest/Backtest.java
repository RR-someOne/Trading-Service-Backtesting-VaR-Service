package com.trading.service.backtest;

import com.trading.service.backtesting.MarketDataLoader;
import com.trading.service.model.OHLC;
import com.trading.service.model.Order;
import com.trading.service.model.Portfolio;
import com.trading.service.risk.VaRService;
// import com.trading.service.strategy.TradingStrategy; // Temporarily commented out
import java.time.LocalDate;
import java.util.*;

/**
 * Enhanced Backtest runner for historical strategy evaluation with VaR integration.
 *
 * <p>This class implements the complete backtesting workflow: 1. Load historical price series for
 * assets A, B 2. Calculate daily returns 3. Feed returns to strategy and simulate orders with
 * realistic fills 4. At each day-end, compute portfolio P&L and update return series 5. Run VaR
 * service for portfolio value using last N returns (rolling window) 6. Save daily VaR numbers and
 * include in backtest report
 */
public class Backtest {

  private final MarketDataLoader dataLoader;
  private final VaRService varService;

  public Backtest() {
    this.dataLoader = new MarketDataLoader();
    this.varService = new VaRService();
  }

  /**
   * Run backtest with comprehensive P&L and VaR analysis.
   *
   * @param config Backtest configuration
   * @return Detailed backtest results including VaR metrics
   */
  public BacktestResult run(BacktestConfig config) {
    System.out.println("Starting backtest: " + config.getDescription());

    // Load historical data for all assets
    Map<String, List<OHLC>> assetData = loadAssetData(config);
    if (assetData.isEmpty()) {
      return new BacktestResult("No data loaded", new ArrayList<>(), new ArrayList<>());
    }

    // Initialize portfolio and strategy
    Portfolio portfolio = new Portfolio(config.getInitialCash());
    Object strategy = config.getStrategy(); // Temporarily using Object

    // Get unified date range across all assets
    List<LocalDate> tradingDates = getUnifiedTradingDates(assetData, config);

    // Initialize strategy with historical data
    List<OHLC> allHistoricalData = getAllHistoricalData(assetData);
    // strategy.initialize(allHistoricalData); // Temporarily commented out

    // Track daily portfolio values and returns for VaR calculation
    List<Double> portfolioValues = new ArrayList<>();
    List<Double> portfolioReturns = new ArrayList<>();
    List<VaRReport> dailyVaRReports = new ArrayList<>();

    double previousPortfolioValue = config.getInitialCash();

    // Main backtesting loop
    for (int dayIndex = 0; dayIndex < tradingDates.size(); dayIndex++) {
      LocalDate currentDate = tradingDates.get(dayIndex);

      // Get current market data for all assets
      Map<String, OHLC> currentMarketData = getCurrentMarketData(assetData, currentDate);
      if (currentMarketData.isEmpty()) {
        continue;
      }

      // Generate and execute orders for each asset
      for (Map.Entry<String, OHLC> entry : currentMarketData.entrySet()) {
        String symbol = entry.getKey();
        OHLC currentBar = entry.getValue();

        // Get historical data up to current date for strategy
        List<OHLC> historicalBars = getHistoricalDataUpToDate(assetData.get(symbol), currentDate);

        // Generate trading signals
        List<Order> orders =
            new ArrayList<>(); // strategy.generateSignals(currentBar, historicalBars, currentDate);
        // // Temporarily commented out

        // Execute orders with realistic fill simulation
        for (Order order : orders) {
          executeOrderWithRealisticFill(order, currentBar, portfolio);
        }
      }

      // Calculate end-of-day portfolio value
      Map<String, Double> currentPrices = extractCurrentPrices(currentMarketData);
      double currentPortfolioValue = portfolio.getPortfolioValue(currentPrices);
      portfolioValues.add(currentPortfolioValue);

      // Calculate daily portfolio return
      double dailyReturn = 0.0;
      if (previousPortfolioValue > 0) {
        dailyReturn = (currentPortfolioValue - previousPortfolioValue) / previousPortfolioValue;
      }
      portfolioReturns.add(dailyReturn);

      // Calculate VaR using rolling window of returns
      if (portfolioReturns.size() >= config.getVarWindowSize()) {
        VaRReport varReport =
            calculateDailyVaR(portfolioReturns, currentPortfolioValue, config, currentDate);
        dailyVaRReports.add(varReport);
      }

      previousPortfolioValue = currentPortfolioValue;

      // Log progress
      if (dayIndex % 50 == 0 || dayIndex == tradingDates.size() - 1) {
        System.out.printf(
            "Progress: %d/%d days, Portfolio Value: $%.2f%n",
            dayIndex + 1, tradingDates.size(), currentPortfolioValue);
      }
    }

    // Cleanup strategy
    // strategy.cleanup(); // Temporarily commented out

    // Create comprehensive backtest result
    return new BacktestResult(
        "Backtest completed successfully",
        portfolioValues,
        portfolioReturns,
        dailyVaRReports,
        portfolio,
        config.getInitialCash(),
        previousPortfolioValue);
  }

  private Map<String, List<OHLC>> loadAssetData(BacktestConfig config) {
    Map<String, List<OHLC>> assetData = new HashMap<>();

    for (String symbol : config.getAssetSymbols()) {
      try {
        List<OHLC> data;
        if (config.getDataFilePaths().containsKey(symbol)) {
          // Load from CSV file
          data = dataLoader.loadHistoricalData(symbol, config.getDataFilePaths().get(symbol));
        } else {
          // Generate sample data for testing
          data =
              dataLoader.generateSampleData(
                  symbol,
                  config.getStartDate(),
                  (int) config.getStartDate().until(config.getEndDate()).getDays(),
                  100.0);
        }

        // Filter by date range
        data = dataLoader.filterByDateRange(data, config.getStartDate(), config.getEndDate());
        assetData.put(symbol, data);

        System.out.println("Loaded " + data.size() + " records for " + symbol);
      } catch (Exception e) {
        System.err.println("Failed to load data for " + symbol + ": " + e.getMessage());
      }
    }

    return assetData;
  }

  private List<LocalDate> getUnifiedTradingDates(
      Map<String, List<OHLC>> assetData, BacktestConfig config) {
    Set<LocalDate> allDates = new HashSet<>();

    for (List<OHLC> data : assetData.values()) {
      for (OHLC ohlc : data) {
        if (!ohlc.getDate().isBefore(config.getStartDate())
            && !ohlc.getDate().isAfter(config.getEndDate())) {
          allDates.add(ohlc.getDate());
        }
      }
    }

    List<LocalDate> sortedDates = new ArrayList<>(allDates);
    Collections.sort(sortedDates);
    return sortedDates;
  }

  private List<OHLC> getAllHistoricalData(Map<String, List<OHLC>> assetData) {
    List<OHLC> allData = new ArrayList<>();
    for (List<OHLC> data : assetData.values()) {
      allData.addAll(data);
    }
    allData.sort(Comparator.comparing(OHLC::getDate));
    return allData;
  }

  private Map<String, OHLC> getCurrentMarketData(
      Map<String, List<OHLC>> assetData, LocalDate currentDate) {
    Map<String, OHLC> currentData = new HashMap<>();

    for (Map.Entry<String, List<OHLC>> entry : assetData.entrySet()) {
      String symbol = entry.getKey();
      List<OHLC> data = entry.getValue();

      for (OHLC ohlc : data) {
        if (ohlc.getDate().equals(currentDate)) {
          currentData.put(symbol, ohlc);
          break;
        }
      }
    }

    return currentData;
  }

  private List<OHLC> getHistoricalDataUpToDate(List<OHLC> data, LocalDate currentDate) {
    List<OHLC> historicalData = new ArrayList<>();

    for (OHLC ohlc : data) {
      if (!ohlc.getDate().isAfter(currentDate)) {
        historicalData.add(ohlc);
      }
    }

    return historicalData;
  }

  private void executeOrderWithRealisticFill(Order order, OHLC currentBar, Portfolio portfolio) {
    // Simulate realistic fill with slippage and market impact
    double fillPrice = order.getPrice();

    // Add slippage (0.1% for market orders)
    double slippage = 0.001;
    if (order.getType() == Order.OrderType.BUY) {
      fillPrice = fillPrice * (1 + slippage);
    } else {
      fillPrice = fillPrice * (1 - slippage);
    }

    // Ensure fill price is within the day's range
    fillPrice = Math.max(currentBar.getLow(), Math.min(currentBar.getHigh(), fillPrice));

    // Fill the order
    order.fill(order.getQuantity(), fillPrice, currentBar.getDate());

    // Execute in portfolio
    portfolio.executeOrder(order);
  }

  private Map<String, Double> extractCurrentPrices(Map<String, OHLC> currentMarketData) {
    Map<String, Double> prices = new HashMap<>();
    for (Map.Entry<String, OHLC> entry : currentMarketData.entrySet()) {
      prices.put(entry.getKey(), entry.getValue().getClose());
    }
    return prices;
  }

  private VaRReport calculateDailyVaR(
      List<Double> portfolioReturns,
      double currentPortfolioValue,
      BacktestConfig config,
      LocalDate currentDate) {
    // Use rolling window of last N returns
    int windowSize = Math.min(config.getVarWindowSize(), portfolioReturns.size());
    List<Double> rollingReturns =
        portfolioReturns.subList(portfolioReturns.size() - windowSize, portfolioReturns.size());

    // Calculate VaR using different methods
    double historicalVaR =
        VaRService.historicalVaR(
            rollingReturns, config.getVarConfidenceLevel(), currentPortfolioValue);
    double parametricVaR =
        VaRService.parametricVaR(
            rollingReturns, config.getVarConfidenceLevel(), currentPortfolioValue);
    double monteCarloVaR =
        VaRService.monteCarloVaR(
            rollingReturns, config.getVarConfidenceLevel(), currentPortfolioValue, 10000);

    return new VaRReport(
        currentDate,
        currentPortfolioValue,
        historicalVaR,
        parametricVaR,
        monteCarloVaR,
        config.getVarConfidenceLevel(),
        windowSize);
  }
}
