package com.trading.service.document_processing;

import java.io.IOException;
import java.net.URI;

/** Extracts plain text and metadata from a source URI. */
public interface DocumentExtractor {
  FinancialDocument extract(String symbol, DocumentType type, URI source) throws IOException;
}
