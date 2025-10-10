package com.trading.service.persistence.featurestore;

import java.util.Map;

/** Placeholder for a Hopsworks-backed feature store integration. */
public class HopsworksFeatureStore implements FeatureStore {
  @Override
  public void ingestClose(String symbol, long timestampEpochMillis, double closePrice) {
    throw new UnsupportedOperationException("Hopsworks integration not implemented");
  }

  @Override
  public Map<String, Double> getFeatures(String symbol, long timestampEpochMillis) {
    throw new UnsupportedOperationException("Hopsworks integration not implemented");
  }
}
