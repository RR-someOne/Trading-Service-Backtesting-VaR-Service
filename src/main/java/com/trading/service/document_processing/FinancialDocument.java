package com.trading.service.document_processing;

import java.time.Instant;
import java.util.Objects;

/** Metadata and content container for a financial document. */
public final class FinancialDocument {
  private final String id;
  private final String symbol;
  private final DocumentType type;
  private final Instant publishedAt;
  private final String source; // URL or provider id
  private final String rawText; // extracted plain text

  public FinancialDocument(
      String id,
      String symbol,
      DocumentType type,
      Instant publishedAt,
      String source,
      String rawText) {
    this.id = Objects.requireNonNull(id);
    this.symbol = Objects.requireNonNull(symbol);
    this.type = Objects.requireNonNull(type);
    this.publishedAt = Objects.requireNonNull(publishedAt);
    this.source = Objects.requireNonNull(source);
    this.rawText = Objects.requireNonNull(rawText);
  }

  public String id() {
    return id;
  }

  public String symbol() {
    return symbol;
  }

  public DocumentType type() {
    return type;
  }

  public Instant publishedAt() {
    return publishedAt;
  }

  public String source() {
    return source;
  }

  public String rawText() {
    return rawText;
  }
}
