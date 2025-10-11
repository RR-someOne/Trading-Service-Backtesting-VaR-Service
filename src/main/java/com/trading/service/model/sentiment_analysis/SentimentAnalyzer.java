package com.trading.service.model.sentiment_analysis;

public interface SentimentAnalyzer extends AutoCloseable {
  /** Analyze input text and return sentiment label + confidence. */
  SentimentResult analyze(String text) throws Exception;

  @Override
  default void close() throws Exception {}
}
