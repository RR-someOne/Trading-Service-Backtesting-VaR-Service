package com.trading.service.persistence.featurestore;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** CSV-backed time-series repository (append-only CSV per symbol: ts,close). */
public class CsvTimeSeriesRepository implements TimeSeriesRepository, AutoCloseable {
  private final Path baseDir;

  public CsvTimeSeriesRepository(Path baseDir) {
    this.baseDir = baseDir;
    try {
      Files.createDirectories(baseDir);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public synchronized void add(String symbol, long ts, double close) {
    Path file = baseDir.resolve(symbol + ".csv");
    String line = ts + "," + close + "\n";
    try {
      Files.write(
          file,
          line.getBytes(),
          java.nio.file.StandardOpenOption.CREATE,
          java.nio.file.StandardOpenOption.APPEND);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public synchronized List<DataPoint> latest(String symbol, long untilTs, int count) {
    Path file = baseDir.resolve(symbol + ".csv");
    if (!Files.exists(file)) return Collections.emptyList();
    List<DataPoint> result = new ArrayList<>();
    try {
      List<String> lines = Files.readAllLines(file);
      for (String line : lines) {
        if (line == null || line.isEmpty()) continue;
        String[] parts = line.split(",");
        if (parts.length < 2) continue;
        long ts = Long.parseLong(parts[0]);
        if (ts <= untilTs) {
          double v = Double.parseDouble(parts[1]);
          result.add(new DataPoint(ts, v));
        }
      }
    } catch (IOException ignored) {
    }
    result.sort((a, b) -> Long.compare(a.ts, b.ts));
    int from = Math.max(0, result.size() - count);
    return new ArrayList<>(result.subList(from, result.size()));
  }

  @Override
  public void close() {}
}
