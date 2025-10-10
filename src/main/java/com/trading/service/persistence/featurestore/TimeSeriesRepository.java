package com.trading.service.persistence.featurestore;

import java.util.List;

/** Minimal time-series repository for close prices. */
public interface TimeSeriesRepository {
  void add(String symbol, long ts, double close);

  /** Return up to 'count' latest points with ts <= untilTs in ascending order by ts. */
  List<DataPoint> latest(String symbol, long untilTs, int count);

  final class DataPoint {
    public final long ts;
    public final double v;

    public DataPoint(long ts, double v) {
      this.ts = ts;
      this.v = v;
    }
  }
}
