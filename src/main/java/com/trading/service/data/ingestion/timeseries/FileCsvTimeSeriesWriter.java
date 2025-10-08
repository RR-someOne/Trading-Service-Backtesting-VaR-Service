package com.trading.service.data.ingestion.timeseries;

import com.trading.service.data.ingestion.model.BarEvent;
import com.trading.service.data.ingestion.model.MarketDataEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

/** Simple CSV writer (not optimized). */
public class FileCsvTimeSeriesWriter implements TimeSeriesWriter {
  private final PrintWriter tickOut;
  private final PrintWriter barOut;

  public FileCsvTimeSeriesWriter(Path directory) {
    try {
      Files.createDirectories(directory);
      tickOut = new PrintWriter(Files.newBufferedWriter(directory.resolve("ticks.csv")));
      barOut = new PrintWriter(Files.newBufferedWriter(directory.resolve("bars.csv")));
      tickOut.println("symbol,timestamp,bid,ask,last,volume");
      barOut.println("symbol,interval,start,end,open,high,low,close,volume");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void writeTick(MarketDataEvent e) {
    tickOut.printf(
        "%s,%d,%.6f,%.6f,%.6f,%.6f%n",
        e.symbol(), e.timestampEpochMillis(), e.bid(), e.ask(), e.last(), e.volume());
  }

  @Override
  public void writeBar(BarEvent e) {
    barOut.printf(
        "%s,%s,%d,%d,%.6f,%.6f,%.6f,%.6f,%.6f%n",
        e.symbol(),
        e.interval(),
        e.startEpochMillis(),
        e.endEpochMillis(),
        e.open(),
        e.high(),
        e.low(),
        e.close(),
        e.volume());
  }

  @Override
  public void close() {
    tickOut.close();
    barOut.close();
  }
}
