package com.trading.service.persistence.featurestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Simple in-memory time-series repository for deterministic tests and prototyping. */
public class InMemoryTimeSeriesRepository implements TimeSeriesRepository {
  private final Map<String, List<DataPoint>> store = new HashMap<>();

  @Override
  public synchronized void add(String symbol, long ts, double close) {
    store.computeIfAbsent(symbol, k -> new ArrayList<>()).add(new DataPoint(ts, close));
  }

  @Override
  public synchronized List<DataPoint> latest(String symbol, long untilTs, int count) {
    List<DataPoint> all = store.getOrDefault(symbol, Collections.emptyList());
    List<DataPoint> filtered = new ArrayList<>();
    for (DataPoint dp : all) {
      if (dp.ts <= untilTs) filtered.add(dp);
    }
    filtered.sort((a, b) -> Long.compare(a.ts, b.ts));
    int from = Math.max(0, filtered.size() - count);
    return new ArrayList<>(filtered.subList(from, filtered.size()));
  }
}
