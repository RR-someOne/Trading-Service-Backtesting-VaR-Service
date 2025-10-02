package com.trading.service.model;

import java.util.List;
import java.util.Map;

/** Service for handling classification signal models and predictions. */
public class ClassificationSignalService {

  private SignalModel model;

  public ClassificationSignalService(SignalModel model) {
    if (model == null || !model.isValid()) {
      throw new IllegalArgumentException("Invalid signal model provided");
    }
    this.model = model;
  }

  /**
   * Validates input features against the model's expected features.
   *
   * @param features the feature values to validate
   * @return true if features are valid for this model
   */
  public boolean validateFeatures(Map<String, Double> features) {
    if (features == null || features.isEmpty()) {
      return false;
    }

    // Check if all required features are present
    for (String requiredFeature : model.getInputFeatures()) {
      if (!features.containsKey(requiredFeature)) {
        return false;
      }
    }

    // Check for reasonable feature values
    for (Map.Entry<String, Double> entry : features.entrySet()) {
      Double value = entry.getValue();
      if (value == null || !Double.isFinite(value)) {
        return false;
      }
    }

    return true;
  }

  /**
   * Validates input feature array against model requirements.
   *
   * @param featureArray array of feature values
   * @return true if the array is valid
   */
  public boolean validateFeatureArray(double[] featureArray) {
    if (featureArray == null) {
      return false;
    }

    if (featureArray.length != model.getFeatureCount()) {
      return false;
    }

    for (double value : featureArray) {
      if (!Double.isFinite(value)) {
        return false;
      }
    }

    return true;
  }

  /**
   * Converts feature map to ordered array based on model's feature order.
   *
   * @param features map of feature names to values
   * @return ordered array of feature values
   */
  public double[] featuresToArray(Map<String, Double> features) {
    if (!validateFeatures(features)) {
      throw new IllegalArgumentException("Invalid features provided");
    }

    List<String> featureOrder = model.getInputFeatures();
    double[] result = new double[featureOrder.size()];

    for (int i = 0; i < featureOrder.size(); i++) {
      result[i] = features.get(featureOrder.get(i));
    }

    return result;
  }

  /**
   * Creates a signal from prediction result.
   *
   * @param prediction the model prediction (-1, 0, 1)
   * @param confidence the prediction confidence [0, 1]
   * @param timestamp the prediction timestamp
   * @return Signal object
   */
  public Signal createSignal(int prediction, double confidence, long timestamp) {
    if (confidence < 0.0 || confidence > 1.0) {
      throw new IllegalArgumentException("Confidence must be between 0 and 1");
    }

    Signal.SignalType signalType = Signal.SignalType.fromValue(prediction);
    return new Signal(signalType, confidence, model.getName(), model.getVersion(), timestamp);
  }

  /**
   * Simulates a classification prediction (placeholder for actual model inference).
   *
   * @param features input features
   * @return simulated signal
   */
  public Signal predictSignal(Map<String, Double> features) {
    if (!validateFeatures(features)) {
      throw new IllegalArgumentException("Invalid features for prediction");
    }

    // Simulate prediction logic (replace with actual model inference)
    // Simple heuristic for demonstration
    double momentum = features.getOrDefault("momentum", 0.0);
    double smaRatio = features.getOrDefault("sma_ratio", 1.0);

    int prediction;
    double confidence;

    if (momentum > 0.02 && smaRatio > 1.05) {
      prediction = 1; // BUY
      confidence = Math.min(0.9, 0.6 + Math.abs(momentum) * 10);
    } else if (momentum < -0.02 && smaRatio < 0.95) {
      prediction = -1; // SELL
      confidence = Math.min(0.9, 0.6 + Math.abs(momentum) * 10);
    } else {
      prediction = 0; // HOLD
      confidence = 0.5;
    }

    return createSignal(prediction, confidence, System.currentTimeMillis());
  }

  public SignalModel getModel() {
    return model;
  }

  public void setModel(SignalModel model) {
    if (model == null || !model.isValid()) {
      throw new IllegalArgumentException("Invalid signal model provided");
    }
    this.model = model;
  }
}
