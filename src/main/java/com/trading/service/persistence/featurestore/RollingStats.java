package com.trading.service.persistence.featurestore;

/** Utilities for rolling statistics and realized volatility. */
public final class RollingStats {
  private RollingStats() {}

  public static double mean(double[] x) {
    if (x == null || x.length == 0) return Double.NaN;
    double s = 0;
    for (double v : x) s += v;
    return s / x.length;
  }

  /** Sample standard deviation (denominator n-1). */
  public static double stddev(double[] x) {
    if (x == null || x.length <= 1) return Double.NaN;
    double m = mean(x);
    double s2 = 0;
    for (double v : x) s2 += (v - m) * (v - m);
    return Math.sqrt(s2 / (x.length - 1));
  }

  /** Annualized realized volatility from a series of close prices. */
  public static double realizedVol(double[] closes, double annualization) {
    if (closes == null || closes.length <= 1) return Double.NaN;
    double[] r = new double[closes.length - 1];
    for (int i = 1; i < closes.length; i++) {
      r[i - 1] = Math.log(closes[i] / closes[i - 1]);
    }
    double sd = stddev(r);
    if (Double.isNaN(sd)) return Double.NaN;
    return sd * Math.sqrt(annualization);
  }
}
