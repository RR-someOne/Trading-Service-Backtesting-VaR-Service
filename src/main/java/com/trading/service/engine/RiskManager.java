package com.trading.service.engine;

import com.trading.service.model.Order;
import com.trading.service.model.Portfolio;

/** Risk checks before executing an order. */
public interface RiskManager {
  /**
   * @param order candidate order
   * @param portfolioValue current portfolio value (mark-to-market)
   * @param currentVaR VaR estimate for given confidence
   * @param portfolio portfolio snapshot
   * @return true if order is approved
   */
  boolean approve(Order order, double portfolioValue, double currentVaR, Portfolio portfolio);
}
