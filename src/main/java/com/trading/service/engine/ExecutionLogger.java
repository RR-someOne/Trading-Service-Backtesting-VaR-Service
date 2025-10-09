package com.trading.service.engine;

/** Lightweight execution logger for trades, portfolio state, and metrics. */
public interface ExecutionLogger {
  void info(String message);

  void warn(String message);

  void trade(String message);

  void portfolio(String message);

  void metric(String name, double value);
}
