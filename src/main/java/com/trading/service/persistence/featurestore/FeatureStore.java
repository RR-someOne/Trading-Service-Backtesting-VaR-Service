package com.trading.service.persistence.featurestore;

import java.util.Map;

/** Deterministic feature store API for rolling statistics and realized volatility. */
public interface FeatureStore {
  /** Ingest a close price for a symbol at a given timestamp (epoch millis). */
  void ingestClose(String symbol, long timestampEpochMillis, double closePrice);

  /**
   * Compute features deterministically using data up to and including the provided timestamp.
   * Returned map contains feature name to value, e.g., rolling_mean_close, rolling_std_close,
   * realized_vol.
   */
  Map<String, Double> getFeatures(String symbol, long timestampEpochMillis);
}
