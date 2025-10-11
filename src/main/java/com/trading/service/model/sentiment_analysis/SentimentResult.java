package com.trading.service.model.sentiment_analysis;

/** Holds the sentiment label and its confidence score (0..1). */
public final class SentimentResult {
  private final SentimentLabel label;
  private final double score;

  public SentimentResult(SentimentLabel label, double score) {
    this.label = label;
    this.score = score;
  }

  public SentimentLabel label() {
    return label;
  }

  public double score() {
    return score;
  }
}
