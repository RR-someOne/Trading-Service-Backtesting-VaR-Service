package com.trading.service.onnx;

import ai.onnxruntime.*;
import java.nio.FloatBuffer;
import java.util.*;

/** ONNX Runtime predictor for machine learning inference in trading applications. */
public class OnnxPredictor {

  private OrtEnvironment env;
  private OrtSession session;
  private String[] inputNames;

  /**
   * Create a new OnnxPredictor with the specified model file.
   *
   * @param modelPath Path to the ONNX model file
   * @throws OrtException If the model cannot be loaded
   * @throws IllegalArgumentException If modelPath is null or empty
   */
  public OnnxPredictor(String modelPath) throws OrtException {
    if (modelPath == null || modelPath.trim().isEmpty()) {
      throw new IllegalArgumentException("Model path cannot be null or empty");
    }

    env = OrtEnvironment.getEnvironment();
    session = env.createSession(modelPath, new OrtSession.SessionOptions());
    inputNames = session.getInputNames().toArray(new String[0]);
  }

  /**
   * Make predictions using the loaded ONNX model.
   *
   * @param features 2D array where each row is a sample and each column is a feature
   * @return Array of prediction results
   * @throws OrtException If prediction fails
   * @throws IllegalArgumentException If features are invalid
   */
  public float[] predict(float[][] features) throws OrtException {
    if (features == null || features.length == 0) {
      throw new IllegalArgumentException("Features cannot be null or empty");
    }

    // Validate feature dimensions
    int batch = features.length;
    int featureDim = features[0].length;

    for (int i = 1; i < batch; i++) {
      if (features[i] == null || features[i].length != featureDim) {
        throw new IllegalArgumentException(
            "All feature arrays must have the same length. Expected: "
                + featureDim
                + ", got: "
                + (features[i] == null ? "null" : features[i].length)
                + " at index "
                + i);
      }
    }

    // Flatten features to 1D array
    float[] flat = new float[batch * featureDim];
    for (int i = 0; i < batch; i++) {
      System.arraycopy(features[i], 0, flat, i * featureDim, featureDim);
    }

    // Create ONNX tensor and run inference
    OnnxTensor inputTensor =
        OnnxTensor.createTensor(env, FloatBuffer.wrap(flat), new long[] {batch, featureDim});
    Map<String, OnnxTensor> inputs = new HashMap<>();
    inputs.put(inputNames[0], inputTensor);

    try (OrtSession.Result out = session.run(inputs)) {
      OnnxValue val = out.get(0);
      float[][] result = (float[][]) val.getValue();

      // Flatten result to 1D array
      int outLen = result.length * result[0].length;
      float[] flatOut = new float[outLen];
      int idx = 0;
      for (float[] row : result) {
        for (float v : row) {
          flatOut[idx++] = v;
        }
      }
      return flatOut;
    } finally {
      inputTensor.close();
    }
  }

  /**
   * Close the ONNX session and release resources.
   *
   * @throws OrtException If closing fails
   */
  public void close() throws OrtException {
    if (session != null) {
      session.close();
    }
    if (env != null) {
      env.close();
    }
  }

  /**
   * Main method for testing ONNX Runtime integration.
   *
   * @param args Command line arguments (not used)
   */
  public static void main(String[] args) {
    System.out.println("ONNX Runtime integration for Trading Service");
    System.out.println("Available providers: " + OrtEnvironment.getAvailableProviders());

    // Example usage would go here if we had a model file
    System.out.println("OnnxPredictor ready for trading signal inference");
  }
}
