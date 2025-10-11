package com.trading.service.model.sentiment_analysis;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class FinBertOnnxSentimentAnalyzer implements SentimentAnalyzer {
  private final OrtEnvironment env;
  private final OrtSession session;

  public FinBertOnnxSentimentAnalyzer(Path onnxModelPath) throws Exception {
    if (!Files.exists(onnxModelPath)) {
      throw new IllegalArgumentException("ONNX model not found: " + onnxModelPath);
    }
    this.env = OrtEnvironment.getEnvironment();
    this.session = env.createSession(onnxModelPath.toString(), new OrtSession.SessionOptions());
  }

  @Override
  public SentimentResult analyze(String text) throws Exception {
    long[] inputIds = dummyTokenize(text);
    long[] attentionMask = new long[inputIds.length];
    for (int i = 0; i < attentionMask.length; i++) attentionMask[i] = 1;

    long[][] inputIds2D = new long[][] {inputIds};
    long[][] attentionMask2D = new long[][] {attentionMask};

    try (OnnxTensor idsTensor = OnnxTensor.createTensor(env, inputIds2D);
        OnnxTensor maskTensor = OnnxTensor.createTensor(env, attentionMask2D)) {
      Map<String, OnnxTensor> inputs = new HashMap<String, OnnxTensor>();
      inputs.put("input_ids", idsTensor);
      inputs.put("attention_mask", maskTensor);
      try (OrtSession.Result result = session.run(inputs)) {
        float[][] logits = (float[][]) result.get(0).getValue();
        float[] probs = softmax(logits[0]);
        int argmax = 0;
        for (int i = 1; i < probs.length; i++) if (probs[i] > probs[argmax]) argmax = i;
        SentimentLabel label;
        if (argmax == 0) label = SentimentLabel.NEGATIVE;
        else if (argmax == 1) label = SentimentLabel.NEUTRAL;
        else if (argmax == 2) label = SentimentLabel.POSITIVE;
        else label = SentimentLabel.NEUTRAL;
        return new SentimentResult(label, probs[argmax]);
      }
    }
  }

  private static float[] softmax(float[] x) {
    double max = Double.NEGATIVE_INFINITY;
    for (float v : x) max = Math.max(max, v);
    double sum = 0;
    double[] exps = new double[x.length];
    for (int i = 0; i < x.length; i++) {
      exps[i] = Math.exp(x[i] - max);
      sum += exps[i];
    }
    float[] out = new float[x.length];
    for (int i = 0; i < x.length; i++) out[i] = (float) (exps[i] / sum);
    return out;
  }

  private static long[] dummyTokenize(String text) {
    String[] parts = text.split("\\s+");
    int n = Math.min(parts.length, 8);
    long[] ids = new long[n + 2];
    ids[0] = 101;
    for (int i = 0; i < n; i++) ids[i + 1] = 200 + (parts[i].hashCode() & 0x7fff) % 100;
    ids[n + 1] = 102;
    return ids;
  }

  @Override
  public void close() throws Exception {
    session.close();
    env.close();
  }
}
