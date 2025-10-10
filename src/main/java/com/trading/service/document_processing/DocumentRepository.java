package com.trading.service.document_processing;

import java.io.IOException;
import java.util.List;

public interface DocumentRepository extends AutoCloseable {
  void save(FinancialDocument doc) throws IOException;

  List<FinancialDocument> findBySymbol(String symbol) throws IOException;

  @Override
  void close() throws IOException;
}
