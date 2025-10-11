package com.trading.service.model.sentiment_analysis;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Lightweight rule-based sentiment analyzer for fallback/testing. Counts occurrences of
 * positive/negative keywords and assigns a label.
 */
public class RuleBasedSentimentAnalyzer implements SentimentAnalyzer {
  private final Set<String> positives;
  private final Set<String> negatives;

  public RuleBasedSentimentAnalyzer() {
    this.positives =
        new HashSet<>(
            Arrays.asList(
                "beat", "growth", "profit", "bullish", "strong", "positive", "record", "upgrade"));
    this.negatives =
        new HashSet<>(
            Arrays.asList(
                "miss", "loss", "bearish", "weak", "negative", "downgrade", "decline", "risk"));
  }

  @Override
  public SentimentResult analyze(String text) {
    if (text == null || text.isEmpty()) {
      return new SentimentResult(SentimentLabel.NEUTRAL, 0.5);
    }
    String[] tokens = text.toLowerCase(Locale.ROOT).split("\\W+");
    int pos = 0, neg = 0;
    for (String t : tokens) {
      if (positives.contains(t)) pos++;
      if (negatives.contains(t)) neg++;
    }
    if (pos == 0 && neg == 0) return new SentimentResult(SentimentLabel.NEUTRAL, 0.5);
    if (pos > neg) {
      double score = Math.min(0.99, 0.5 + (pos - neg) * 0.1);
      return new SentimentResult(SentimentLabel.POSITIVE, score);
    } else if (neg > pos) {
      double score = Math.min(0.99, 0.5 + (neg - pos) * 0.1);
      return new SentimentResult(SentimentLabel.NEGATIVE, score);
    } else {
      return new SentimentResult(SentimentLabel.NEUTRAL, 0.6);
    }
  }
}
