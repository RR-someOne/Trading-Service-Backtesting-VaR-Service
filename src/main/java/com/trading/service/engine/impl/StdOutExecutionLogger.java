package com.trading.service.engine.impl;

import com.trading.service.engine.ExecutionLogger;

/** Minimal logger that prints to stdout. Replace with SLF4J in production. */
public class StdOutExecutionLogger implements ExecutionLogger {
  @Override
  public void info(String message) {
    System.out.println("[INFO] " + message);
  }

  @Override
  public void warn(String message) {
    System.out.println("[WARN] " + message);
  }

  @Override
  public void trade(String message) {
    System.out.println("[TRADE] " + message);
  }

  @Override
  public void portfolio(String message) {
    System.out.println("[PORTFOLIO] " + message);
  }

  @Override
  public void metric(String name, double value) {
    System.out.println("[METRIC] " + name + "=" + value);
  }
}
