package com.trading.service.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Java interface for M2M100 Multi-Language Translation Model Integrates with Python-based
 * transformer model for trading content translation
 */
public class TranslationService {

  private final ObjectMapper objectMapper;
  private final Map<String, String> translationCache;
  private final String pythonScriptPath;
  private final boolean cachingEnabled;

  /**
   * Initialize Translation Service
   *
   * @param pythonScriptPath Path to the Python translation script
   * @param cachingEnabled Whether to enable translation caching
   */
  public TranslationService(String pythonScriptPath, boolean cachingEnabled) {
    this.objectMapper = new ObjectMapper();
    this.translationCache = new ConcurrentHashMap<>();
    this.pythonScriptPath = pythonScriptPath;
    this.cachingEnabled = cachingEnabled;
  }

  /** Default constructor with standard configuration */
  public TranslationService() {
    this(
        "src/main/java/com/trading/service/model/TransformerLanguageTranslation/language_translation_models.py",
        true);
  }

  /**
   * Translate trading signal text
   *
   * @param signalText The trading signal to translate
   * @param sourceLanguage Source language code (e.g., "en")
   * @param targetLanguage Target language code (e.g., "es")
   * @return Translated signal text
   * @throws TranslationException If translation fails
   */
  public String translateTradingSignal(
      String signalText, String sourceLanguage, String targetLanguage) throws TranslationException {
    TranslationRequest request =
        new TranslationRequest.Builder()
            .text(signalText)
            .sourceLanguage(sourceLanguage)
            .targetLanguage(targetLanguage)
            .context("trading_signal")
            .priority("high")
            .preserveNumbers(true)
            .preserveTradingTerms(true)
            .build();

    TranslationResponse response = translate(request);
    return response.getTranslatedText();
  }

  /**
   * Translate market analysis with detailed response
   *
   * @param analysisText The market analysis text to translate
   * @param sourceLanguage Source language code
   * @param targetLanguage Target language code
   * @return Translation response with confidence and metadata
   * @throws TranslationException If translation fails
   */
  public TranslationResponse translateMarketAnalysis(
      String analysisText, String sourceLanguage, String targetLanguage)
      throws TranslationException {
    TranslationRequest request =
        new TranslationRequest.Builder()
            .text(analysisText)
            .sourceLanguage(sourceLanguage)
            .targetLanguage(targetLanguage)
            .context("market_analysis")
            .priority("normal")
            .preserveNumbers(true)
            .preserveTradingTerms(true)
            .build();

    return translate(request);
  }

  /**
   * Translate VaR and risk reports
   *
   * @param reportText The risk report text to translate
   * @param sourceLanguage Source language code
   * @param targetLanguage Target language code
   * @return Translation response with high accuracy metadata
   * @throws TranslationException If translation fails
   */
  public TranslationResponse translateRiskReport(
      String reportText, String sourceLanguage, String targetLanguage) throws TranslationException {
    TranslationRequest request =
        new TranslationRequest.Builder()
            .text(reportText)
            .sourceLanguage(sourceLanguage)
            .targetLanguage(targetLanguage)
            .context("risk_report")
            .priority("urgent")
            .preserveNumbers(true)
            .preserveTradingTerms(true)
            .build();

    return translate(request);
  }

  /**
   * Batch translate multiple texts
   *
   * @param requests List of translation requests
   * @return CompletableFuture with list of translation responses
   */
  public CompletableFuture<List<TranslationResponse>> batchTranslate(
      List<TranslationRequest> requests) {
    return CompletableFuture.supplyAsync(
        () -> {
          return requests.stream()
              .map(
                  request -> {
                    try {
                      return translate(request);
                    } catch (TranslationException e) {
                      return TranslationResponse.error(request, e.getMessage());
                    }
                  })
              .toList();
        });
  }

  /**
   * Core translation method
   *
   * @param request Translation request object
   * @return Translation response
   * @throws TranslationException If translation fails
   */
  private TranslationResponse translate(TranslationRequest request) throws TranslationException {
    // Check cache first
    String cacheKey = generateCacheKey(request);
    if (cachingEnabled && translationCache.containsKey(cacheKey)) {
      return deserializeResponse(translationCache.get(cacheKey));
    }

    try {
      // Prepare JSON input for Python script
      String jsonInput = objectMapper.writeValueAsString(request.toMap());

      // Execute Python translation script
      ProcessBuilder processBuilder =
          new ProcessBuilder("python3", pythonScriptPath, "--json-input", jsonInput);
      processBuilder.redirectErrorStream(true);

      Process process = processBuilder.start();

      // Read output
      StringBuilder output = new StringBuilder();
      try (BufferedReader reader =
          new BufferedReader(new InputStreamReader(process.getInputStream()))) {
        String line;
        while ((line = reader.readLine()) != null) {
          output.append(line).append("\n");
        }
      }

      // Wait for process completion
      int exitCode = process.waitFor();
      if (exitCode != 0) {
        throw new TranslationException("Python script failed with exit code: " + exitCode);
      }

      // Parse response
      String responseJson = output.toString().trim();
      TranslationResponse response = deserializeResponse(responseJson);

      // Cache successful translation
      if (cachingEnabled && response.isSuccess()) {
        translationCache.put(cacheKey, responseJson);
      }

      return response;

    } catch (IOException | InterruptedException e) {
      throw new TranslationException("Failed to execute translation: " + e.getMessage(), e);
    }
  }

  /**
   * Get list of supported languages
   *
   * @return List of supported language codes
   */
  public List<String> getSupportedLanguages() {
    return List.of(
        "en", "es", "fr", "de", "it", "pt", "ru", "zh", "ja", "ko", "ar", "hi", "nl", "sv", "no",
        "da", "fi", "pl", "cs", "hu");
  }

  /**
   * Validate if language pair is supported
   *
   * @param sourceLanguage Source language code
   * @param targetLanguage Target language code
   * @return true if language pair is supported
   */
  public boolean validateLanguagePair(String sourceLanguage, String targetLanguage) {
    List<String> supported = getSupportedLanguages();
    return supported.contains(sourceLanguage) && supported.contains(targetLanguage);
  }

  /** Clear translation cache */
  public void clearCache() {
    translationCache.clear();
  }

  /**
   * Get cache statistics
   *
   * @return Map with cache statistics
   */
  public Map<String, Object> getCacheStats() {
    return Map.of(
        "size", translationCache.size(),
        "enabled", cachingEnabled,
        "keys", translationCache.keySet());
  }

  // Helper methods

  private String generateCacheKey(TranslationRequest request) {
    return String.format(
        "%s_%s_%s_%s",
        request.getText().hashCode(),
        request.getSourceLanguage(),
        request.getTargetLanguage(),
        request.getContext());
  }

  private TranslationResponse deserializeResponse(String json) throws TranslationException {
    try {
      JsonNode node = objectMapper.readTree(json);

      if (node.has("error")) {
        return TranslationResponse.error(null, node.get("error").asText());
      }

      return TranslationResponse.builder()
          .originalText(node.get("original").asText(""))
          .translatedText(node.get("translated").asText(""))
          .sourceLanguage(node.get("source_language").asText(""))
          .targetLanguage(node.get("target_language").asText(""))
          .confidenceScore(node.get("confidence").asDouble(0.0))
          .processingTime(node.get("processing_time").asDouble(0.0))
          .modelVersion(node.get("model_version").asText(""))
          .success(true)
          .build();

    } catch (IOException e) {
      throw new TranslationException("Failed to parse translation response", e);
    }
  }

  /** Translation request builder class */
  public static class TranslationRequest {
    private String text;
    private String sourceLanguage;
    private String targetLanguage;
    private String context;
    private String priority;
    private boolean preserveNumbers;
    private boolean preserveTradingTerms;

    // Getters
    public String getText() {
      return text;
    }

    public String getSourceLanguage() {
      return sourceLanguage;
    }

    public String getTargetLanguage() {
      return targetLanguage;
    }

    public String getContext() {
      return context;
    }

    public String getPriority() {
      return priority;
    }

    public boolean isPreserveNumbers() {
      return preserveNumbers;
    }

    public boolean isPreserveTradingTerms() {
      return preserveTradingTerms;
    }

    public Map<String, Object> toMap() {
      return Map.of(
          "text", text,
          "source_language", sourceLanguage,
          "target_language", targetLanguage,
          "context", context,
          "priority", priority,
          "preserve_numbers", preserveNumbers,
          "preserve_trading_terms", preserveTradingTerms);
    }

    public static class Builder {
      private final TranslationRequest request = new TranslationRequest();

      public Builder text(String text) {
        request.text = text;
        return this;
      }

      public Builder sourceLanguage(String language) {
        request.sourceLanguage = language;
        return this;
      }

      public Builder targetLanguage(String language) {
        request.targetLanguage = language;
        return this;
      }

      public Builder context(String context) {
        request.context = context;
        return this;
      }

      public Builder priority(String priority) {
        request.priority = priority;
        return this;
      }

      public Builder preserveNumbers(boolean preserve) {
        request.preserveNumbers = preserve;
        return this;
      }

      public Builder preserveTradingTerms(boolean preserve) {
        request.preserveTradingTerms = preserve;
        return this;
      }

      public TranslationRequest build() {
        return request;
      }
    }
  }

  /** Translation response class */
  public static class TranslationResponse {
    private String originalText;
    private String translatedText;
    private String sourceLanguage;
    private String targetLanguage;
    private double confidenceScore;
    private double processingTime;
    private String modelVersion;
    private boolean success;
    private String errorMessage;

    // Getters
    public String getOriginalText() {
      return originalText;
    }

    public String getTranslatedText() {
      return translatedText;
    }

    public String getSourceLanguage() {
      return sourceLanguage;
    }

    public String getTargetLanguage() {
      return targetLanguage;
    }

    public double getConfidenceScore() {
      return confidenceScore;
    }

    public double getProcessingTime() {
      return processingTime;
    }

    public String getModelVersion() {
      return modelVersion;
    }

    public boolean isSuccess() {
      return success;
    }

    public String getErrorMessage() {
      return errorMessage;
    }

    public static Builder builder() {
      return new Builder();
    }

    public static TranslationResponse error(TranslationRequest request, String errorMessage) {
      return builder()
          .originalText(request != null ? request.getText() : "")
          .translatedText("")
          .sourceLanguage(request != null ? request.getSourceLanguage() : "")
          .targetLanguage(request != null ? request.getTargetLanguage() : "")
          .success(false)
          .errorMessage(errorMessage)
          .build();
    }

    public static class Builder {
      private final TranslationResponse response = new TranslationResponse();

      public Builder originalText(String text) {
        response.originalText = text;
        return this;
      }

      public Builder translatedText(String text) {
        response.translatedText = text;
        return this;
      }

      public Builder sourceLanguage(String language) {
        response.sourceLanguage = language;
        return this;
      }

      public Builder targetLanguage(String language) {
        response.targetLanguage = language;
        return this;
      }

      public Builder confidenceScore(double score) {
        response.confidenceScore = score;
        return this;
      }

      public Builder processingTime(double time) {
        response.processingTime = time;
        return this;
      }

      public Builder modelVersion(String version) {
        response.modelVersion = version;
        return this;
      }

      public Builder success(boolean success) {
        response.success = success;
        return this;
      }

      public Builder errorMessage(String message) {
        response.errorMessage = message;
        return this;
      }

      public TranslationResponse build() {
        return response;
      }
    }
  }

  /** Translation exception class */
  public static class TranslationException extends Exception {
    public TranslationException(String message) {
      super(message);
    }

    public TranslationException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
