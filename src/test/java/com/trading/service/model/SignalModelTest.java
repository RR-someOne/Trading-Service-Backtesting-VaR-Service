package com.trading.service.model;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

/** Unit tests for SignalModel class. */
public class SignalModelTest {

  private SignalModel validModel;
  private Map<String, String> labelMapping;
  private List<String> inputFeatures;

  @Before
  public void setUp() {
    labelMapping = new HashMap<>();
    labelMapping.put("-1", "SELL");
    labelMapping.put("0", "HOLD");
    labelMapping.put("1", "BUY");

    inputFeatures =
        Arrays.asList("return", "log_return", "sma_ratio", "vol_short", "vol_long", "momentum");

    validModel =
        new SignalModel(
            "rf_signal",
            "v1",
            "scikit-learn",
            "2025-09-30T12:00:00Z",
            inputFeatures,
            labelMapping,
            "RandomForestClassifier 200 trees, scaler included in pipeline");
  }

  @Test
  public void testValidModelCreation() {
    assertNotNull("Model should not be null", validModel);
    assertEquals("rf_signal", validModel.getName());
    assertEquals("v1", validModel.getVersion());
    assertEquals("scikit-learn", validModel.getFramework());
    assertEquals(6, validModel.getFeatureCount());
    assertTrue("Model should be valid", validModel.isValid());
  }

  @Test
  public void testInvalidModelWithNullName() {
    SignalModel invalidModel =
        new SignalModel(
            null,
            "v1",
            "scikit-learn",
            "2025-09-30T12:00:00Z",
            inputFeatures,
            labelMapping,
            "notes");
    assertFalse("Model with null name should be invalid", invalidModel.isValid());
  }

  @Test
  public void testInvalidModelWithEmptyName() {
    SignalModel invalidModel =
        new SignalModel(
            "", "v1", "scikit-learn", "2025-09-30T12:00:00Z", inputFeatures, labelMapping, "notes");
    assertFalse("Model with empty name should be invalid", invalidModel.isValid());
  }

  @Test
  public void testInvalidModelWithNullFeatures() {
    SignalModel invalidModel =
        new SignalModel(
            "rf_signal", "v1", "scikit-learn", "2025-09-30T12:00:00Z", null, labelMapping, "notes");
    assertFalse("Model with null features should be invalid", invalidModel.isValid());
  }

  @Test
  public void testInvalidModelWithEmptyFeatures() {
    SignalModel invalidModel =
        new SignalModel(
            "rf_signal",
            "v1",
            "scikit-learn",
            "2025-09-30T12:00:00Z",
            Arrays.asList(),
            labelMapping,
            "notes");
    assertFalse("Model with empty features should be invalid", invalidModel.isValid());
  }

  @Test
  public void testInvalidModelWithNullLabelMapping() {
    SignalModel invalidModel =
        new SignalModel(
            "rf_signal",
            "v1",
            "scikit-learn",
            "2025-09-30T12:00:00Z",
            inputFeatures,
            null,
            "notes");
    assertFalse("Model with null label mapping should be invalid", invalidModel.isValid());
  }

  @Test
  public void testInvalidModelWithEmptyLabelMapping() {
    SignalModel invalidModel =
        new SignalModel(
            "rf_signal",
            "v1",
            "scikit-learn",
            "2025-09-30T12:00:00Z",
            inputFeatures,
            new HashMap<>(),
            "notes");
    assertFalse("Model with empty label mapping should be invalid", invalidModel.isValid());
  }

  @Test
  public void testFeatureCount() {
    assertEquals("Feature count should be 6", 6, validModel.getFeatureCount());

    SignalModel emptyFeatureModel = new SignalModel();
    emptyFeatureModel.setInputFeatures(null);
    assertEquals(
        "Empty feature model should have 0 features", 0, emptyFeatureModel.getFeatureCount());
  }

  @Test
  public void testHasFeature() {
    assertTrue("Model should have 'return' feature", validModel.hasFeature("return"));
    assertTrue("Model should have 'momentum' feature", validModel.hasFeature("momentum"));
    assertFalse(
        "Model should not have 'unknown_feature'", validModel.hasFeature("unknown_feature"));
    assertFalse("Model should not have null feature", validModel.hasFeature(null));
  }

  @Test
  public void testGetSignalLabels() {
    List<String> signalLabels = validModel.getSignalLabels();
    assertNotNull("Signal labels should not be null", signalLabels);
    assertEquals("Should have 3 signal labels", 3, signalLabels.size());
    assertTrue("Should contain BUY label", signalLabels.contains("BUY"));
    assertTrue("Should contain SELL label", signalLabels.contains("SELL"));
    assertTrue("Should contain HOLD label", signalLabels.contains("HOLD"));
  }

  @Test
  public void testGetSignalLabelsWithNullMapping() {
    SignalModel modelWithNullMapping = new SignalModel();
    modelWithNullMapping.setLabelMapping(null);
    List<String> signalLabels = modelWithNullMapping.getSignalLabels();
    assertNotNull("Signal labels should not be null even with null mapping", signalLabels);
    assertTrue("Signal labels should be empty with null mapping", signalLabels.isEmpty());
  }

  @Test
  public void testSettersAndGetters() {
    SignalModel model = new SignalModel();

    // Test setters
    model.setName("test_model");
    model.setVersion("v2");
    model.setFramework("tensorflow");
    model.setCreatedAt("2025-10-01T12:00:00Z");
    model.setInputFeatures(Arrays.asList("feature1", "feature2"));
    model.setLabelMapping(labelMapping);
    model.setNotes("Test notes");

    // Test getters
    assertEquals("test_model", model.getName());
    assertEquals("v2", model.getVersion());
    assertEquals("tensorflow", model.getFramework());
    assertEquals("2025-10-01T12:00:00Z", model.getCreatedAt());
    assertEquals(2, model.getInputFeatures().size());
    assertEquals(labelMapping, model.getLabelMapping());
    assertEquals("Test notes", model.getNotes());
  }

  @Test
  public void testToString() {
    String modelString = validModel.toString();
    assertNotNull("toString should not return null", modelString);
    assertTrue("toString should contain model name", modelString.contains("rf_signal"));
    assertTrue("toString should contain version", modelString.contains("v1"));
    assertTrue("toString should contain framework", modelString.contains("scikit-learn"));
    assertTrue("toString should contain feature count", modelString.contains("6"));
  }

  @Test
  public void testDefaultConstructor() {
    SignalModel model = new SignalModel();
    assertNotNull("Default constructor should create non-null object", model);
    assertFalse("Default model should be invalid", model.isValid());
    assertEquals("Default model should have 0 features", 0, model.getFeatureCount());
  }
}
