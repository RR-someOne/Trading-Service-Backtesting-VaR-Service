package com.trading.service.document_processing;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Objects;

/** Coordinates ingestion of financial documents and persistence. */
public class DocumentIngestionService implements AutoCloseable {
  private final DocumentExtractor extractor;
  private final DocumentRepository repository;

  public DocumentIngestionService(DocumentExtractor extractor, DocumentRepository repository) {
    this.extractor = Objects.requireNonNull(extractor);
    this.repository = Objects.requireNonNull(repository);
  }

  public void ingest(String symbol, DocumentType type, List<URI> sources) throws IOException {
    for (URI src : sources) {
      FinancialDocument doc = extractor.extract(symbol, type, src);
      repository.save(doc);
    }
  }

  @Override
  public void close() throws IOException {
    repository.close();
  }
}
