package com.trading.service.strategy;

import com.trading.service.model.OHLC;
import com.trading.service.model.Order;
import java.time.LocalDate;
import java.util.List;

/**
 * Interface for trading strategies used in backtesting. Strategies receive OHLC data and generate
 * trading signals/orders.
 */
public interface TradingStrategy {

  /**
   * Initialize the strategy with historical data for analysis. Called once before backtesting
   * starts.
   *
   * @param historicalData Historical OHLC data for strategy initialization
   */
  void initialize(List<OHLC> historicalData);

  /**
   * Generate trading signals based on current market data. Called for each day during backtesting.
   *
   * @param currentBar Current day's OHLC data
   * @param historicalBars Previous OHLC data (including current bar)
   * @param currentDate Current trading date
   * @return List of orders to execute (can be empty)
   */
  List<Order> generateSignals(OHLC currentBar, List<OHLC> historicalBars, LocalDate currentDate);

  /**
   * Get strategy name for reporting purposes.
   *
   * @return Strategy name
   */
  String getStrategyName();

  /** Clean up resources when backtesting is complete. */
  default void cleanup() {
    // Default implementation does nothing
  }
}
