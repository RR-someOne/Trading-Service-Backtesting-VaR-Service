package com.trading.service.engine;

import com.trading.service.model.Order;

/** Abstraction over a broker/exchange execution API. */
public interface Broker extends AutoCloseable {
  Order submit(Order order);

  @Override
  default void close() {}
}
