package com.trading.service.strategy;

import com.trading.service.model.OHLC;
import com.trading.service.model.Order;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Simple moving average crossover strategy. Generates buy signals when short MA crosses above long
 * MA, and sell signals when short MA crosses below long MA.
 */
public class MovingAverageCrossoverStrategy implements TradingStrategy {

  private final int shortPeriod;
  private final int longPeriod;
  private final int positionSize;
  private final String strategyName;

  private boolean isLongPosition = false;

  public MovingAverageCrossoverStrategy(int shortPeriod, int longPeriod, int positionSize) {
    this.shortPeriod = shortPeriod;
    this.longPeriod = longPeriod;
    this.positionSize = positionSize;
    this.strategyName = String.format("MA_Crossover_%d_%d", shortPeriod, longPeriod);
  }

  @Override
  public void initialize(List<OHLC> historicalData) {
    // Reset position state
    isLongPosition = false;
    System.out.println(
        "Initialized " + getStrategyName() + " with " + historicalData.size() + " historical bars");
  }

  @Override
  public List<Order> generateSignals(
      OHLC currentBar, List<OHLC> historicalBars, LocalDate currentDate) {
    List<Order> orders = new ArrayList<>();

    // Need enough data for long MA calculation
    if (historicalBars.size() < longPeriod) {
      return orders;
    }

    // Calculate moving averages
    double shortMA = calculateMovingAverage(historicalBars, shortPeriod);
    double longMA = calculateMovingAverage(historicalBars, longPeriod);

    // Get previous MAs for crossover detection
    if (historicalBars.size() >= longPeriod + 1) {
      List<OHLC> previousBars = historicalBars.subList(0, historicalBars.size() - 1);
      double prevShortMA = calculateMovingAverage(previousBars, shortPeriod);
      double prevLongMA = calculateMovingAverage(previousBars, longPeriod);

      // Detect crossovers
      boolean bullishCrossover = (prevShortMA <= prevLongMA) && (shortMA > longMA);
      boolean bearishCrossover = (prevShortMA >= prevLongMA) && (shortMA < longMA);

      // Generate buy signal
      if (bullishCrossover && !isLongPosition) {
        String orderId =
            "BUY_" + currentBar.getSymbol() + "_" + UUID.randomUUID().toString().substring(0, 8);
        Order buyOrder =
            new Order(
                orderId,
                currentBar.getSymbol(),
                Order.OrderType.BUY,
                positionSize,
                currentBar.getClose(),
                currentDate);
        orders.add(buyOrder);
        isLongPosition = true;
        System.out.println(
            "Generated BUY signal for "
                + currentBar.getSymbol()
                + " on "
                + currentDate
                + " at price "
                + currentBar.getClose());
      }

      // Generate sell signal
      if (bearishCrossover && isLongPosition) {
        String orderId =
            "SELL_" + currentBar.getSymbol() + "_" + UUID.randomUUID().toString().substring(0, 8);
        Order sellOrder =
            new Order(
                orderId,
                currentBar.getSymbol(),
                Order.OrderType.SELL,
                positionSize,
                currentBar.getClose(),
                currentDate);
        orders.add(sellOrder);
        isLongPosition = false;
        System.out.println(
            "Generated SELL signal for "
                + currentBar.getSymbol()
                + " on "
                + currentDate
                + " at price "
                + currentBar.getClose());
      }
    }

    return orders;
  }

  /** Calculate simple moving average for the last n periods. */
  private double calculateMovingAverage(List<OHLC> bars, int periods) {
    if (bars.size() < periods) {
      return 0.0;
    }

    double sum = 0.0;
    int startIdx = bars.size() - periods;

    for (int i = startIdx; i < bars.size(); i++) {
      sum += bars.get(i).getClose();
    }

    return sum / periods;
  }

  @Override
  public String getStrategyName() {
    return strategyName;
  }

  @Override
  public void cleanup() {
    isLongPosition = false;
    System.out.println("Cleaned up " + getStrategyName());
  }
}

/** Simple buy-and-hold strategy for comparison. */
class BuyAndHoldStrategy implements TradingStrategy {

  private final int positionSize;
  private boolean hasPosition = false;

  public BuyAndHoldStrategy(int positionSize) {
    this.positionSize = positionSize;
  }

  @Override
  public void initialize(List<OHLC> historicalData) {
    hasPosition = false;
    System.out.println("Initialized Buy-and-Hold strategy");
  }

  @Override
  public List<Order> generateSignals(
      OHLC currentBar, List<OHLC> historicalBars, LocalDate currentDate) {
    List<Order> orders = new ArrayList<>();

    // Buy on first day only
    if (!hasPosition) {
      String orderId =
          "BUY_HOLD_" + currentBar.getSymbol() + "_" + UUID.randomUUID().toString().substring(0, 8);
      Order buyOrder =
          new Order(
              orderId,
              currentBar.getSymbol(),
              Order.OrderType.BUY,
              positionSize,
              currentBar.getClose(),
              currentDate);
      orders.add(buyOrder);
      hasPosition = true;
      System.out.println(
          "Buy-and-Hold: Bought "
              + currentBar.getSymbol()
              + " on "
              + currentDate
              + " at price "
              + currentBar.getClose());
    }

    return orders;
  }

  @Override
  public String getStrategyName() {
    return "Buy_and_Hold";
  }

  @Override
  public void cleanup() {
    hasPosition = false;
  }
}
