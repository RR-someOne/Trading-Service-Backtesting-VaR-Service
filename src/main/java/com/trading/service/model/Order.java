package com.trading.service.model;

import java.time.LocalDate;

/** Represents a trading order with execution details. */
public class Order {
  public enum OrderType {
    BUY,
    SELL
  }

  public enum OrderStatus {
    PENDING,
    FILLED,
    PARTIAL_FILL,
    CANCELLED
  }

  private final String orderId;
  private final String symbol;
  private final OrderType type;
  private final int quantity;
  private final double price;
  private final LocalDate orderDate;
  private OrderStatus status;
  private int filledQuantity;
  private double fillPrice;
  private LocalDate fillDate;

  public Order(
      String orderId,
      String symbol,
      OrderType type,
      int quantity,
      double price,
      LocalDate orderDate) {
    this.orderId = orderId;
    this.symbol = symbol;
    this.type = type;
    this.quantity = quantity;
    this.price = price;
    this.orderDate = orderDate;
    this.status = OrderStatus.PENDING;
    this.filledQuantity = 0;
    this.fillPrice = 0.0;
  }

  public void fill(int fillQty, double fillPrice, LocalDate fillDate) {
    this.filledQuantity += fillQty;
    this.fillPrice = fillPrice;
    this.fillDate = fillDate;

    if (filledQuantity >= quantity) {
      this.status = OrderStatus.FILLED;
    } else if (filledQuantity > 0) {
      this.status = OrderStatus.PARTIAL_FILL;
    }
  }

  // Getters
  public String getOrderId() {
    return orderId;
  }

  public String getSymbol() {
    return symbol;
  }

  public OrderType getType() {
    return type;
  }

  public int getQuantity() {
    return quantity;
  }

  public double getPrice() {
    return price;
  }

  public LocalDate getOrderDate() {
    return orderDate;
  }

  public OrderStatus getStatus() {
    return status;
  }

  public int getFilledQuantity() {
    return filledQuantity;
  }

  public double getFillPrice() {
    return fillPrice;
  }

  public LocalDate getFillDate() {
    return fillDate;
  }

  @Override
  public String toString() {
    return String.format(
        "Order{id='%s', symbol='%s', type=%s, qty=%d, price=%.2f, status=%s}",
        orderId, symbol, type, quantity, price, status);
  }
}
