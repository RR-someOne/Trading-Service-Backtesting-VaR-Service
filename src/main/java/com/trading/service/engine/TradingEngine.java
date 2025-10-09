package com.trading.service.engine;

import com.trading.service.model.Order;
import com.trading.service.model.Portfolio;
import com.trading.service.model.Signal;
import com.trading.service.risk.VaRService;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/** Core trading engine entry point for live or simulated execution. */
public class TradingEngine {
  private final Portfolio portfolio;
  private final Broker broker;
  private final RiskManager riskManager;
  private final ExecutionLogger logger;

  // state
  private volatile double lastComputedVaR;
  private volatile double realizedPnL;

  public TradingEngine(
      Portfolio portfolio, Broker broker, RiskManager riskManager, ExecutionLogger logger) {
    this.portfolio = Objects.requireNonNull(portfolio);
    this.broker = Objects.requireNonNull(broker);
    this.riskManager = Objects.requireNonNull(riskManager);
    this.logger = Objects.requireNonNull(logger);
  }

  /**
   * Process an actionable signal into an order, apply risk checks, execute, and update state.
   *
   * @param symbol instrument symbol
   * @param signal trading signal
   * @param quantity desired order quantity (positive integer)
   * @param limitPrice execution price assumption (simulation) or limit price
   * @param recentReturns most recent portfolio returns for VaR calculation
   * @param varConfidence VaR confidence level (e.g., 0.95)
   * @param currentPrices current price map for portfolio valuation
   * @return true if executed, false if rejected
   */
  public boolean processSignal(
      String symbol,
      Signal signal,
      int quantity,
      double limitPrice,
      List<Double> recentReturns,
      double varConfidence,
      Map<String, Double> currentPrices) {
    if (signal == null || !signal.isActionable() || quantity <= 0 || limitPrice <= 0) {
      logger.info("Non-actionable or invalid signal ignored: " + signal);
      return false;
    }

    Order.OrderType side =
        signal.getType() == Signal.SignalType.BUY ? Order.OrderType.BUY : Order.OrderType.SELL;
    Order candidate =
        new Order(
            UUID.randomUUID().toString(), symbol, side, quantity, limitPrice, LocalDate.now());

    double portfolioValue = portfolio.getPortfolioValue(currentPrices);
    double var = VaRService.parametricVaR(recentReturns, varConfidence, portfolioValue);
    this.lastComputedVaR = var;

    if (!riskManager.approve(candidate, portfolioValue, var, portfolio)) {
      logger.warn("Order rejected by risk manager: " + candidate);
      return false;
    }

    // Capture avg cost BEFORE portfolio mutation for accurate realized PnL
    Double avgCostBefore = portfolio.getAverageCosts().get(symbol);
    Order executed = broker.submit(candidate);
    // Update portfolio positions if filled
    portfolio.executeOrder(executed);

    // Update simple realized PnL on sells using average cost basis at time of trade
    if (executed.getStatus() == Order.OrderStatus.FILLED
        && executed.getType() == Order.OrderType.SELL) {
      double basis =
          avgCostBefore != null ? avgCostBefore : executed.getFillPrice(); // fallback if unknown
      realizedPnL += (executed.getFillPrice() - basis) * executed.getFilledQuantity();
    }

    logger.trade("Executed: " + executed.toString());
    logger.portfolio("State: " + portfolio.toString());
    logger.metric("VaR", var);
    return true;
  }

  public double getLastComputedVaR() {
    return lastComputedVaR;
  }

  public double getRealizedPnL() {
    return realizedPnL;
  }
}
