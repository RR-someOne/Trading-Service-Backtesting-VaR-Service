package com.trading.service.model;

import java.util.List;
import java.util.Map;

/** Represents a classification signal model configuration. */
public class SignalModel {

  private String name;
  private String version;
  private String framework;
  private String createdAt;
  private List<String> inputFeatures;
  private Map<String, String> labelMapping;
  private String notes;

  public SignalModel() {}

  public SignalModel(
      String name,
      String version,
      String framework,
      String createdAt,
      List<String> inputFeatures,
      Map<String, String> labelMapping,
      String notes) {
    this.name = name;
    this.version = version;
    this.framework = framework;
    this.createdAt = createdAt;
    this.inputFeatures = inputFeatures;
    this.labelMapping = labelMapping;
    this.notes = notes;
  }

  // Getters
  public String getName() {
    return name;
  }

  public String getVersion() {
    return version;
  }

  public String getFramework() {
    return framework;
  }

  public String getCreatedAt() {
    return createdAt;
  }

  public List<String> getInputFeatures() {
    return inputFeatures;
  }

  public Map<String, String> getLabelMapping() {
    return labelMapping;
  }

  public String getNotes() {
    return notes;
  }

  // Setters
  public void setName(String name) {
    this.name = name;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public void setFramework(String framework) {
    this.framework = framework;
  }

  public void setCreatedAt(String createdAt) {
    this.createdAt = createdAt;
  }

  public void setInputFeatures(List<String> inputFeatures) {
    this.inputFeatures = inputFeatures;
  }

  public void setLabelMapping(Map<String, String> labelMapping) {
    this.labelMapping = labelMapping;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  /**
   * Validates that the model configuration is complete and valid.
   *
   * @return true if valid, false otherwise
   */
  public boolean isValid() {
    return name != null
        && !name.trim().isEmpty()
        && version != null
        && !version.trim().isEmpty()
        && framework != null
        && !framework.trim().isEmpty()
        && inputFeatures != null
        && !inputFeatures.isEmpty()
        && labelMapping != null
        && !labelMapping.isEmpty();
  }

  /**
   * Gets the expected number of input features.
   *
   * @return number of input features
   */
  public int getFeatureCount() {
    return inputFeatures != null ? inputFeatures.size() : 0;
  }

  /**
   * Checks if the model supports a specific feature.
   *
   * @param feature the feature name to check
   * @return true if the feature is supported
   */
  public boolean hasFeature(String feature) {
    return inputFeatures != null && inputFeatures.contains(feature);
  }

  /**
   * Gets all possible signal labels.
   *
   * @return list of signal labels (BUY, SELL, HOLD)
   */
  public List<String> getSignalLabels() {
    return labelMapping != null ? List.copyOf(labelMapping.values()) : List.of();
  }

  @Override
  public String toString() {
    return "SignalModel{"
        + "name='"
        + name
        + '\''
        + ", version='"
        + version
        + '\''
        + ", framework='"
        + framework
        + '\''
        + ", featureCount="
        + getFeatureCount()
        + '}';
  }
}
