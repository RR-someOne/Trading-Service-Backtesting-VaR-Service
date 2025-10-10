package com.trading.service.document_processing;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import org.junit.Test;

public class FileDocumentRepositoryTest {

  @Test
  public void saveAndFindBySymbol_roundTrip() throws Exception {
    Path tmp = Files.createTempDirectory("docrepo-test");
    Path file = tmp.resolve("documents.ndjson");

    try (FileDocumentRepository repo = new FileDocumentRepository(file)) {
      FinancialDocument d1 =
          new FinancialDocument(
              "id-1",
              "AAPL",
              DocumentType.TEN_K,
              Instant.parse("2024-01-01T00:00:00Z"),
              "http://example.com/aapl/10k",
              "AAPL 10-K content");
      FinancialDocument d2 =
          new FinancialDocument(
              "id-2",
              "MSFT",
              DocumentType.TEN_Q,
              Instant.parse("2024-02-01T00:00:00Z"),
              "http://example.com/msft/10q",
              "MSFT 10-Q content");

      repo.save(d1);
      repo.save(d2);
    }

    try (FileDocumentRepository repo2 = new FileDocumentRepository(file)) {
      List<FinancialDocument> aapl = repo2.findBySymbol("AAPL");
      assertThat(aapl.size(), is(1));
      FinancialDocument out = aapl.get(0);
      assertThat(out.id(), is("id-1"));
      assertThat(out.symbol(), is("AAPL"));
      assertThat(out.type(), is(DocumentType.TEN_K));
      assertThat(out.source(), is("http://example.com/aapl/10k"));
      assertThat(out.rawText(), containsString("AAPL 10-K"));
    }
  }
}
