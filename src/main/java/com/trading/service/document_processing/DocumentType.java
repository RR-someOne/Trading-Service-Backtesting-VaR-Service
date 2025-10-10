package com.trading.service.document_processing;

/** Types of financial documents supported by the ingestion pipeline. */
public enum DocumentType {
  TEN_K,
  TEN_Q,
  EARNINGS_RELEASE,
  PRESS_RELEASE,
  OTHER
}
