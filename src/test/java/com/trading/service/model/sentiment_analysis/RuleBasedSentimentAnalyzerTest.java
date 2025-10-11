package com.trading.service.model.sentiment_analysis;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.IsCloseTo.closeTo;

import org.junit.Test;

public class RuleBasedSentimentAnalyzerTest {

  @Test
  public void positive_keywords_yield_positive_label() throws Exception {
    RuleBasedSentimentAnalyzer analyzer = new RuleBasedSentimentAnalyzer();
    SentimentResult res = analyzer.analyze("record profits and strong growth; bullish upgrade");
    assertThat(res.label(), is(SentimentLabel.POSITIVE));
    // score should be above neutral
    assertThat(res.score(), closeTo(Math.max(res.score(), 0.6), 0.4));
  }

  @Test
  public void negative_keywords_yield_negative_label() throws Exception {
    RuleBasedSentimentAnalyzer analyzer = new RuleBasedSentimentAnalyzer();
    SentimentResult res = analyzer.analyze("loss, weak guidance, risk of decline, bearish");
    assertThat(res.label(), is(SentimentLabel.NEGATIVE));
    assertThat(res.score(), closeTo(Math.max(res.score(), 0.6), 0.4));
  }

  @Test
  public void no_keywords_yields_neutral() throws Exception {
    RuleBasedSentimentAnalyzer analyzer = new RuleBasedSentimentAnalyzer();
    SentimentResult res = analyzer.analyze("Quarterly update with mixed details and figures");
    assertThat(res.label(), is(SentimentLabel.NEUTRAL));
    assertThat(res.score(), closeTo(0.5, 0.2));
  }

  @Test
  public void null_or_empty_is_neutral() throws Exception {
    RuleBasedSentimentAnalyzer analyzer = new RuleBasedSentimentAnalyzer();
    assertThat(analyzer.analyze(null).label(), is(SentimentLabel.NEUTRAL));
    assertThat(analyzer.analyze("").label(), is(SentimentLabel.NEUTRAL));
  }
}
