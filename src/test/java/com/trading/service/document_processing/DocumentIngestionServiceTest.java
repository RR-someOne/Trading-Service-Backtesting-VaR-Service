package com.trading.service.document_processing;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.Test;

public class DocumentIngestionServiceTest {

  static class FakeExtractor implements DocumentExtractor {
    @Override
    public FinancialDocument extract(String symbol, DocumentType type, URI source) {
      return new FinancialDocument(
          UUID.randomUUID().toString(), symbol, type, Instant.EPOCH, source.toString(), "body");
    }
  }

  @Test
  public void ingest_savesAllSources() throws Exception {
    Path tmp = Files.createTempDirectory("docingest-test");
    Path file = tmp.resolve("documents.ndjson");

    DocumentExtractor extractor = new FakeExtractor();
    try (FileDocumentRepository repo = new FileDocumentRepository(file);
        DocumentIngestionService svc = new DocumentIngestionService(extractor, repo)) {
      svc.ingest(
          "AAPL",
          DocumentType.EARNINGS_RELEASE,
          List.of(new URI("http://example.com/r1"), new URI("http://example.com/r2")));
    }

    try (FileDocumentRepository repo2 = new FileDocumentRepository(file)) {
      assertThat(repo2.findBySymbol("AAPL").size(), is(2));
    }
  }
}
