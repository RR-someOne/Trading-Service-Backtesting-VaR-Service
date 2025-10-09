package com.trading.service.engine.impl;

import com.trading.service.engine.Broker;
import com.trading.service.model.Order;
import java.time.LocalDate;

/** Simple simulated broker: fills immediately at requested price. */
public class SimpleBroker implements Broker {
  @Override
  public Order submit(Order order) {
    // Fill immediately
    order.fill(order.getQuantity(), order.getPrice(), LocalDate.now());
    return order;
  }
}
