package com.trading.service.persistence.featurestore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import org.junit.Test;

public class InHouseFeatureStoreTest {

  @Test
  public void rollingStatsAndVolAreDeterministic() {
    InMemoryTimeSeriesRepository repo = new InMemoryTimeSeriesRepository();
    InHouseFeatureStore fs = new InHouseFeatureStore(repo, 5, 252.0);
    String sym = "TEST";
    long t = 1_000L;
    double[] closes = new double[] {100, 101, 102, 101, 103, 104};
    for (double c : closes) {
      fs.ingestClose(sym, t, c);
      t += 1000;
    }
    long queryTs = 1_000L + 5 * 1000; // include all 6 points, last 5 considered
    Map<String, Double> f = fs.getFeatures(sym, queryTs);

    // Last 5 closes: 101,102,101,103,104
    double mean = (101 + 102 + 101 + 103 + 104) / 5.0;
    assertEquals(mean, f.get(FeatureKeys.ROLLING_MEAN_CLOSE), 1e-9);

    // Sample std dev of those 5 values
    double[] x = new double[] {101, 102, 101, 103, 104};
    double m = mean;
    double s2 = 0;
    for (double v : x) s2 += (v - m) * (v - m);
    double sd = Math.sqrt(s2 / (x.length - 1));
    assertEquals(sd, f.get(FeatureKeys.ROLLING_STD_CLOSE), 1e-9);

    // Realized vol: stddev of log returns over those 5 closes -> 4 returns
    double[] r =
        new double[] {
          Math.log(102.0 / 101.0),
          Math.log(101.0 / 102.0),
          Math.log(103.0 / 101.0),
          Math.log(104.0 / 103.0)
        };
    double rm = 0;
    for (double v : r) rm += v;
    rm /= r.length;
    double rs2 = 0;
    for (double v : r) rs2 += (v - rm) * (v - rm);
    double rsd = Math.sqrt(rs2 / (r.length - 1));
    double realized = rsd * Math.sqrt(252.0);
    assertEquals(realized, f.get(FeatureKeys.REALIZED_VOL), 1e-9);

    // Basic sanity
    assertTrue(f.get(FeatureKeys.REALIZED_VOL) > 0);
  }
}
