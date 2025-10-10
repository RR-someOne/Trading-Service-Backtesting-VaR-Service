package com.trading.service.persistence.featurestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Minimal in-house feature store computing rolling stats and realized volatility. */
public class InHouseFeatureStore implements FeatureStore {
  private final TimeSeriesRepository repo;
  private final int windowSize;
  private final double annualization;

  public InHouseFeatureStore(TimeSeriesRepository repo, int windowSize, double annualization) {
    this.repo = repo;
    this.windowSize = Math.max(2, windowSize);
    this.annualization = annualization;
  }

  @Override
  public void ingestClose(String symbol, long timestampEpochMillis, double closePrice) {
    repo.add(symbol, timestampEpochMillis, closePrice);
  }

  @Override
  public Map<String, Double> getFeatures(String symbol, long timestampEpochMillis) {
    List<TimeSeriesRepository.DataPoint> dps =
        repo.latest(symbol, timestampEpochMillis, windowSize);
    double[] closes = new double[dps.size()];
    for (int i = 0; i < dps.size(); i++) closes[i] = dps.get(i).v;

    Map<String, Double> out = new HashMap<>();
    out.put(FeatureKeys.ROLLING_MEAN_CLOSE, RollingStats.mean(closes));
    out.put(FeatureKeys.ROLLING_STD_CLOSE, RollingStats.stddev(closes));
    out.put(FeatureKeys.REALIZED_VOL, RollingStats.realizedVol(closes, annualization));
    return out;
  }
}
