package com.trading.service.model;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

/** Unit tests for ClassificationSignalService class. */
public class ClassificationSignalServiceTest {

  private ClassificationSignalService service;
  private SignalModel validModel;
  private Map<String, Double> validFeatures;

  @Before
  public void setUp() {
    // Create a valid signal model
    Map<String, String> labelMapping = new HashMap<>();
    labelMapping.put("-1", "SELL");
    labelMapping.put("0", "HOLD");
    labelMapping.put("1", "BUY");

    List<String> inputFeatures =
        Arrays.asList("return", "log_return", "sma_ratio", "vol_short", "vol_long", "momentum");

    validModel =
        new SignalModel(
            "rf_signal",
            "v1",
            "scikit-learn",
            "2025-09-30T12:00:00Z",
            inputFeatures,
            labelMapping,
            "RandomForestClassifier");

    service = new ClassificationSignalService(validModel);

    // Create valid feature set
    validFeatures = new HashMap<>();
    validFeatures.put("return", 0.015);
    validFeatures.put("log_return", 0.0149);
    validFeatures.put("sma_ratio", 1.05);
    validFeatures.put("vol_short", 0.12);
    validFeatures.put("vol_long", 0.18);
    validFeatures.put("momentum", 0.025);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithNullModel() {
    new ClassificationSignalService(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithInvalidModel() {
    SignalModel invalidModel = new SignalModel();
    new ClassificationSignalService(invalidModel);
  }

  @Test
  public void testValidateFeatures() {
    assertTrue("Valid features should pass validation", service.validateFeatures(validFeatures));
  }

  @Test
  public void testValidateFeaturesWithNullFeatures() {
    assertFalse("Null features should fail validation", service.validateFeatures(null));
  }

  @Test
  public void testValidateFeaturesWithEmptyFeatures() {
    assertFalse("Empty features should fail validation", service.validateFeatures(new HashMap<>()));
  }

  @Test
  public void testValidateFeaturesWithMissingFeature() {
    Map<String, Double> incompleteFeatures = new HashMap<>(validFeatures);
    incompleteFeatures.remove("momentum");
    assertFalse(
        "Features missing required fields should fail validation",
        service.validateFeatures(incompleteFeatures));
  }

  @Test
  public void testValidateFeaturesWithNullValue() {
    Map<String, Double> featuresWithNull = new HashMap<>(validFeatures);
    featuresWithNull.put("momentum", null);
    assertFalse(
        "Features with null values should fail validation",
        service.validateFeatures(featuresWithNull));
  }

  @Test
  public void testValidateFeaturesWithInfiniteValue() {
    Map<String, Double> featuresWithInf = new HashMap<>(validFeatures);
    featuresWithInf.put("momentum", Double.POSITIVE_INFINITY);
    assertFalse(
        "Features with infinite values should fail validation",
        service.validateFeatures(featuresWithInf));
  }

  @Test
  public void testValidateFeaturesWithNaNValue() {
    Map<String, Double> featuresWithNaN = new HashMap<>(validFeatures);
    featuresWithNaN.put("momentum", Double.NaN);
    assertFalse(
        "Features with NaN values should fail validation",
        service.validateFeatures(featuresWithNaN));
  }

  @Test
  public void testValidateFeatureArray() {
    double[] validArray = {0.015, 0.0149, 1.05, 0.12, 0.18, 0.025};
    assertTrue(
        "Valid feature array should pass validation", service.validateFeatureArray(validArray));
  }

  @Test
  public void testValidateFeatureArrayWithNull() {
    assertFalse("Null feature array should fail validation", service.validateFeatureArray(null));
  }

  @Test
  public void testValidateFeatureArrayWithWrongLength() {
    double[] wrongLengthArray = {0.015, 0.0149, 1.05}; // Only 3 features instead of 6
    assertFalse(
        "Feature array with wrong length should fail validation",
        service.validateFeatureArray(wrongLengthArray));
  }

  @Test
  public void testValidateFeatureArrayWithInfiniteValue() {
    double[] arrayWithInf = {0.015, 0.0149, 1.05, 0.12, 0.18, Double.POSITIVE_INFINITY};
    assertFalse(
        "Feature array with infinite values should fail validation",
        service.validateFeatureArray(arrayWithInf));
  }

  @Test
  public void testValidateFeatureArrayWithNaNValue() {
    double[] arrayWithNaN = {0.015, 0.0149, 1.05, 0.12, 0.18, Double.NaN};
    assertFalse(
        "Feature array with NaN values should fail validation",
        service.validateFeatureArray(arrayWithNaN));
  }

  @Test
  public void testFeaturesToArray() {
    double[] result = service.featuresToArray(validFeatures);

    assertNotNull("Result should not be null", result);
    assertEquals("Result should have correct length", 6, result.length);

    // Check values are in correct order based on model's input features
    assertEquals("return should be first", 0.015, result[0], 0.001);
    assertEquals("log_return should be second", 0.0149, result[1], 0.001);
    assertEquals("sma_ratio should be third", 1.05, result[2], 0.001);
    assertEquals("vol_short should be fourth", 0.12, result[3], 0.001);
    assertEquals("vol_long should be fifth", 0.18, result[4], 0.001);
    assertEquals("momentum should be sixth", 0.025, result[5], 0.001);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFeaturesToArrayWithInvalidFeatures() {
    Map<String, Double> invalidFeatures = new HashMap<>();
    invalidFeatures.put("invalid_feature", 0.5);
    service.featuresToArray(invalidFeatures);
  }

  @Test
  public void testCreateSignal() {
    long timestamp = System.currentTimeMillis();
    Signal signal = service.createSignal(1, 0.8, timestamp);

    assertNotNull("Signal should not be null", signal);
    assertEquals("Signal type should be BUY", Signal.SignalType.BUY, signal.getType());
    assertEquals("Confidence should match", 0.8, signal.getConfidence(), 0.001);
    assertEquals("Model name should match", "rf_signal", signal.getModelName());
    assertEquals("Model version should match", "v1", signal.getModelVersion());
    assertEquals("Timestamp should match", timestamp, signal.getTimestamp());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateSignalWithInvalidConfidenceHigh() {
    service.createSignal(1, 1.5, System.currentTimeMillis());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateSignalWithInvalidConfidenceLow() {
    service.createSignal(1, -0.1, System.currentTimeMillis());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateSignalWithInvalidPrediction() {
    service.createSignal(99, 0.8, System.currentTimeMillis());
  }

  @Test
  public void testPredictSignalBuyCondition() {
    // Set up features that should trigger BUY signal
    Map<String, Double> buyFeatures = new HashMap<>(validFeatures);
    buyFeatures.put("momentum", 0.03); // High positive momentum
    buyFeatures.put("sma_ratio", 1.1); // Price above SMA

    Signal signal = service.predictSignal(buyFeatures);

    assertNotNull("Signal should not be null", signal);
    assertEquals("Should predict BUY signal", Signal.SignalType.BUY, signal.getType());
    assertTrue("Confidence should be reasonable", signal.getConfidence() > 0.5);
    assertTrue("BUY signal should be actionable", signal.isActionable());
  }

  @Test
  public void testPredictSignalSellCondition() {
    // Set up features that should trigger SELL signal
    Map<String, Double> sellFeatures = new HashMap<>(validFeatures);
    sellFeatures.put("momentum", -0.03); // High negative momentum
    sellFeatures.put("sma_ratio", 0.9); // Price below SMA

    Signal signal = service.predictSignal(sellFeatures);

    assertNotNull("Signal should not be null", signal);
    assertEquals("Should predict SELL signal", Signal.SignalType.SELL, signal.getType());
    assertTrue("Confidence should be reasonable", signal.getConfidence() > 0.5);
    assertTrue("SELL signal should be actionable", signal.isActionable());
  }

  @Test
  public void testPredictSignalHoldCondition() {
    // Set up features that should trigger HOLD signal
    Map<String, Double> holdFeatures = new HashMap<>(validFeatures);
    holdFeatures.put("momentum", 0.01); // Low momentum
    holdFeatures.put("sma_ratio", 1.0); // Price at SMA

    Signal signal = service.predictSignal(holdFeatures);

    assertNotNull("Signal should not be null", signal);
    assertEquals("Should predict HOLD signal", Signal.SignalType.HOLD, signal.getType());
    assertFalse("HOLD signal should not be actionable", signal.isActionable());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testPredictSignalWithInvalidFeatures() {
    Map<String, Double> invalidFeatures = new HashMap<>();
    invalidFeatures.put("wrong_feature", 0.5);
    service.predictSignal(invalidFeatures);
  }

  @Test
  public void testGetAndSetModel() {
    assertEquals("Should return the original model", validModel, service.getModel());

    // Create a new valid model
    Map<String, String> newLabelMapping = new HashMap<>();
    newLabelMapping.put("-1", "SELL");
    newLabelMapping.put("0", "HOLD");
    newLabelMapping.put("1", "BUY");

    SignalModel newModel =
        new SignalModel(
            "new_model",
            "v2",
            "tensorflow",
            "2025-10-01T12:00:00Z",
            Arrays.asList("feature1", "feature2"),
            newLabelMapping,
            "New model");

    service.setModel(newModel);
    assertEquals("Should return the new model", newModel, service.getModel());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetModelWithNull() {
    service.setModel(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetModelWithInvalidModel() {
    SignalModel invalidModel = new SignalModel();
    service.setModel(invalidModel);
  }
}
