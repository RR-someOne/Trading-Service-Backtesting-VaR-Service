package com.trading.service.data.ingestion.archive;

import com.trading.service.data.ingestion.config.IngestionConfig;
import com.trading.service.data.ingestion.model.BarEvent;
import com.trading.service.data.ingestion.model.MarketDataEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Simple local file archiver batching raw JSON lines (placeholder for S3). */
public class LocalFileBatchArchiver implements BatchArchiver {
  private final int batchSize;
  private final List<String> buffer = new ArrayList<>();
  private final Path outFile;

  public LocalFileBatchArchiver(IngestionConfig cfg, Path directory) {
    try {
      Files.createDirectories(directory);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    this.batchSize = cfg.getArchiverBatchSize();
    this.outFile = directory.resolve("archive.ndjson");
  }

  @Override
  public void acceptTick(MarketDataEvent e) {
    buffer.add("T," + e.symbol() + "," + e.timestampEpochMillis());
    maybeFlush();
  }

  @Override
  public void acceptBar(BarEvent e) {
    buffer.add("B," + e.symbol() + "," + e.startEpochMillis());
    maybeFlush();
  }

  private void maybeFlush() {
    if (buffer.size() >= batchSize) flush();
  }

  @Override
  public synchronized void flush() {
    if (buffer.isEmpty()) return;
    try (PrintWriter pw =
        new PrintWriter(
            Files.newBufferedWriter(
                outFile,
                java.nio.file.StandardOpenOption.CREATE,
                java.nio.file.StandardOpenOption.APPEND))) {
      for (String line : buffer) pw.println(line);
      buffer.clear();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void close() {
    flush();
  }
}
