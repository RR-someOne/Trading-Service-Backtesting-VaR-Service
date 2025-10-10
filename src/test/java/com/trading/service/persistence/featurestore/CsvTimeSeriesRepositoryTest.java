package com.trading.service.persistence.featurestore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.Test;

public class CsvTimeSeriesRepositoryTest {

  @Test
  public void appendAndLatestWorksWithFiltersAndAscendingOrder() throws Exception {
    Path temp = Files.createTempDirectory("csv-ts-repo-test");
    CsvTimeSeriesRepository repo = new CsvTimeSeriesRepository(temp);
    String symbol = "TEST";

    // out of order writes
    repo.add(symbol, 3L, 103.0);
    repo.add(symbol, 1L, 101.0);
    repo.add(symbol, 2L, 102.0);
    repo.add(symbol, 5L, 105.0);
    repo.add(symbol, 4L, 104.0);

    // latest up to ts=4 with count=3 should return [2,3,4]
    List<TimeSeriesRepository.DataPoint> dps = repo.latest(symbol, 4L, 3);
    assertEquals(3, dps.size());
    assertTrue(dps.get(0).ts < dps.get(1).ts && dps.get(1).ts < dps.get(2).ts);
    assertEquals(2L, dps.get(0).ts);
    assertEquals(3L, dps.get(1).ts);
    assertEquals(4L, dps.get(2).ts);

    // latest up to ts=5 with count=10 should return [1,2,3,4,5]
    dps = repo.latest(symbol, 5L, 10);
    assertEquals(5, dps.size());
    for (int i = 1; i < dps.size(); i++) {
      assertTrue(dps.get(i - 1).ts < dps.get(i).ts);
    }
    assertEquals(1L, dps.get(0).ts);
    assertEquals(5L, dps.get(4).ts);
  }
}
