package com.trading.service.model.sentiment_analysis;

import java.nio.file.Path;
import org.junit.Test;

public class FinBertOnnxSentimentAnalyzerTest {
  @Test(expected = IllegalArgumentException.class)
  public void constructor_throws_if_model_missing() throws Exception {
    // Use a definitely-missing path to avoid touching ONNX
    new FinBertOnnxSentimentAnalyzer(Path.of("/no/such/model.onnx"));
  }
}
