package com.trading.service.persistence.featurestore;

/** Canonical feature names produced by the in-house feature store. */
public final class FeatureKeys {
  private FeatureKeys() {}

  public static final String ROLLING_MEAN_CLOSE = "rolling_mean_close";
  public static final String ROLLING_STD_CLOSE = "rolling_std_close";
  public static final String REALIZED_VOL = "realized_vol"; // annualized stdev of log returns
}
