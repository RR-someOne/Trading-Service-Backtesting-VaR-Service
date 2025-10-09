package com.trading.service.engine.impl;

import com.trading.service.engine.RiskManager;
import com.trading.service.model.Order;
import com.trading.service.model.Portfolio;

/** Basic risk rules: max order notional and VaR cap. */
public class SimpleRiskManager implements RiskManager {
  private final double maxOrderNotional;
  private final double maxVaR;

  public SimpleRiskManager(double maxOrderNotional, double maxVaR) {
    this.maxOrderNotional = maxOrderNotional;
    this.maxVaR = maxVaR;
  }

  @Override
  public boolean approve(
      Order order, double portfolioValue, double currentVaR, Portfolio portfolio) {
    double notional = Math.abs(order.getQuantity() * order.getPrice());
    if (notional > maxOrderNotional) return false;
    if (currentVaR > maxVaR) return false;
    // Optional: basic cash/short checks could be added here
    return true;
  }
}
