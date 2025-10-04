package com.trading.service.model;

import java.util.HashMap;
import java.util.Map;

/** Portfolio representing holdings and cash position. */
public class Portfolio {
  private double cash;
  private final Map<String, Integer> positions; // symbol -> quantity
  private final Map<String, Double> averageCosts; // symbol -> average cost basis

  public Portfolio(double initialCash) {
    this.cash = initialCash;
    this.positions = new HashMap<>();
    this.averageCosts = new HashMap<>();
  }

  public void executeOrder(Order order) {
    if (order.getStatus() != Order.OrderStatus.FILLED) {
      return;
    }

    String symbol = order.getSymbol();
    int quantity = order.getFilledQuantity();
    double price = order.getFillPrice();
    double totalCost = quantity * price;

    if (order.getType() == Order.OrderType.BUY) {
      // Buy order
      cash -= totalCost;
      int currentPosition = positions.getOrDefault(symbol, 0);
      double currentAvgCost = averageCosts.getOrDefault(symbol, 0.0);

      // Update average cost
      if (currentPosition > 0) {
        double totalCost_new = (currentPosition * currentAvgCost) + totalCost;
        int totalQuantity = currentPosition + quantity;
        averageCosts.put(symbol, totalCost_new / totalQuantity);
      } else {
        averageCosts.put(symbol, price);
      }

      positions.put(symbol, currentPosition + quantity);
    } else {
      // Sell order
      cash += totalCost;
      int currentPosition = positions.getOrDefault(symbol, 0);
      positions.put(symbol, currentPosition - quantity);

      // Remove from holdings if position becomes zero
      if (positions.get(symbol) <= 0) {
        positions.remove(symbol);
        averageCosts.remove(symbol);
      }
    }
  }

  public double getPortfolioValue(Map<String, Double> currentPrices) {
    double totalValue = cash;

    for (Map.Entry<String, Integer> position : positions.entrySet()) {
      String symbol = position.getKey();
      int quantity = position.getValue();
      double currentPrice = currentPrices.getOrDefault(symbol, 0.0);
      totalValue += quantity * currentPrice;
    }

    return totalValue;
  }

  public double getCash() {
    return cash;
  }

  public Map<String, Integer> getPositions() {
    return new HashMap<>(positions);
  }

  public Map<String, Double> getAverageCosts() {
    return new HashMap<>(averageCosts);
  }

  @Override
  public String toString() {
    return String.format("Portfolio{cash=%.2f, positions=%s}", cash, positions);
  }
}
