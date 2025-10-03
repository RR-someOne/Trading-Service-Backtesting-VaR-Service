package com.trading.service.model;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * Simplified Translation Service for M2M100 integration Works with existing Trading Service
 * dependencies without additional JSON libraries
 */
public class SimpleTranslationService {

  private final String pythonScriptPath;
  private static final int TIMEOUT_SECONDS = 30;

  /**
   * Initialize Simple Translation Service
   *
   * @param pythonScriptPath Path to the Python translation CLI script
   */
  public SimpleTranslationService(String pythonScriptPath) {
    this.pythonScriptPath = pythonScriptPath;
  }

  /** Default constructor with standard script path */
  public SimpleTranslationService() {
    this(
        "src/main/java/com/trading/service/model/TransformerLanguageTranslation/translation_cli.py");
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
    return executeTranslation(signalText, sourceLanguage, targetLanguage, "trading_signal");
  }

  /**
   * Translate market analysis text
   *
   * @param analysisText The market analysis text to translate
   * @param sourceLanguage Source language code
   * @param targetLanguage Target language code
   * @return Translated analysis text
   * @throws TranslationException If translation fails
   */
  public String translateMarketAnalysis(
      String analysisText, String sourceLanguage, String targetLanguage)
      throws TranslationException {
    return executeTranslation(analysisText, sourceLanguage, targetLanguage, "market_analysis");
  }

  /**
   * Translate VaR and risk reports
   *
   * @param reportText The risk report text to translate
   * @param sourceLanguage Source language code
   * @param targetLanguage Target language code
   * @return Translated report text
   * @throws TranslationException If translation fails
   */
  public String translateRiskReport(String reportText, String sourceLanguage, String targetLanguage)
      throws TranslationException {
    return executeTranslation(reportText, sourceLanguage, targetLanguage, "risk_report");
  }

  /**
   * Test the translation service
   *
   * @return Test results as string
   * @throws TranslationException If test fails
   */
  public String testTranslation() throws TranslationException {
    try {
      ProcessBuilder processBuilder = new ProcessBuilder("python3", pythonScriptPath, "--test");
      processBuilder.redirectErrorStream(true);

      Process process = processBuilder.start();

      String output = readProcessOutput(process);

      boolean finished = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
      if (!finished) {
        process.destroyForcibly();
        throw new TranslationException("Translation test timed out");
      }

      if (process.exitValue() != 0) {
        throw new TranslationException("Translation test failed: " + output);
      }

      return output;

    } catch (IOException | InterruptedException e) {
      throw new TranslationException("Failed to execute translation test: " + e.getMessage(), e);
    }
  }

  /**
   * Check if the translation service is available
   *
   * @return true if service is available
   */
  public boolean isAvailable() {
    try {
      ProcessBuilder processBuilder = new ProcessBuilder("python3", "--version");
      Process process = processBuilder.start();
      boolean finished = process.waitFor(5, TimeUnit.SECONDS);
      return finished && process.exitValue() == 0;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Core translation execution method
   *
   * @param text Text to translate
   * @param sourceLanguage Source language code
   * @param targetLanguage Target language code
   * @param context Translation context
   * @return Translated text
   * @throws TranslationException If translation fails
   */
  private String executeTranslation(
      String text, String sourceLanguage, String targetLanguage, String context)
      throws TranslationException {
    if (text == null || text.trim().isEmpty()) {
      return text;
    }

    try {
      ProcessBuilder processBuilder =
          new ProcessBuilder(
              "python3", pythonScriptPath,
              "--text", text,
              "--source-lang", sourceLanguage,
              "--target-lang", targetLanguage,
              "--context", context);
      processBuilder.redirectErrorStream(true);

      Process process = processBuilder.start();

      String output = readProcessOutput(process);

      boolean finished = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
      if (!finished) {
        process.destroyForcibly();
        throw new TranslationException("Translation timed out");
      }

      if (process.exitValue() != 0) {
        throw new TranslationException("Translation failed: " + output);
      }

      // Parse simple JSON response to extract translated text
      return extractTranslatedText(output);

    } catch (IOException | InterruptedException e) {
      throw new TranslationException("Failed to execute translation: " + e.getMessage(), e);
    }
  }

  /** Read process output */
  private String readProcessOutput(Process process) throws IOException {
    StringBuilder output = new StringBuilder();
    try (BufferedReader reader =
        new BufferedReader(
            new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        output.append(line).append("\n");
      }
    }
    return output.toString().trim();
  }

  /** Extract translated text from JSON response (simple parsing) */
  private String extractTranslatedText(String jsonOutput) throws TranslationException {
    // Simple JSON parsing without external libraries
    // Look for "translated_text": "value" pattern

    if (jsonOutput.contains("\"error\"")) {
      throw new TranslationException("Translation service returned error: " + jsonOutput);
    }

    String pattern = "\"translated_text\":";
    int startIndex = jsonOutput.indexOf(pattern);
    if (startIndex == -1) {
      throw new TranslationException("Invalid translation response format");
    }

    startIndex += pattern.length();

    // Skip whitespace and opening quote
    while (startIndex < jsonOutput.length()
        && (jsonOutput.charAt(startIndex) == ' '
            || jsonOutput.charAt(startIndex) == '\t'
            || jsonOutput.charAt(startIndex) == '\n')) {
      startIndex++;
    }

    if (startIndex >= jsonOutput.length() || jsonOutput.charAt(startIndex) != '"') {
      throw new TranslationException("Invalid translation response format");
    }

    startIndex++; // Skip opening quote

    // Find closing quote
    int endIndex = startIndex;
    while (endIndex < jsonOutput.length()) {
      if (jsonOutput.charAt(endIndex) == '"'
          && (endIndex == startIndex || jsonOutput.charAt(endIndex - 1) != '\\')) {
        break;
      }
      endIndex++;
    }

    if (endIndex >= jsonOutput.length()) {
      throw new TranslationException("Invalid translation response format");
    }

    return jsonOutput.substring(startIndex, endIndex);
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

  /** Example usage and testing */
  public static void main(String[] args) {
    SimpleTranslationService translator = new SimpleTranslationService();

    try {
      // Check if service is available
      if (!translator.isAvailable()) {
        System.out.println("Python not available. Please install Python 3.8+");
        return;
      }

      System.out.println("Testing M2M100 Translation Service...");

      // Test trading signal translation
      String signal = "BUY EURUSD at 1.1850, Stop Loss: 1.1800, Take Profit: 1.1950";
      System.out.println("Original Signal: " + signal);

      try {
        String translatedSignal = translator.translateTradingSignal(signal, "en", "es");
        System.out.println("Spanish Translation: " + translatedSignal);
      } catch (TranslationException e) {
        System.out.println(
            "Translation failed (this is expected if dependencies not installed): "
                + e.getMessage());
      }

      // Test market analysis translation
      String analysis = "The EUR/USD pair shows strong bullish momentum with RSI above 70.";
      System.out.println("\nOriginal Analysis: " + analysis);

      try {
        String translatedAnalysis = translator.translateMarketAnalysis(analysis, "en", "fr");
        System.out.println("French Translation: " + translatedAnalysis);
      } catch (TranslationException e) {
        System.out.println(
            "Translation failed (this is expected if dependencies not installed): "
                + e.getMessage());
      }

      // Test risk report translation
      String riskReport = "Portfolio VaR at 95% confidence level: $125,000.";
      System.out.println("\nOriginal Risk Report: " + riskReport);

      try {
        String translatedReport = translator.translateRiskReport(riskReport, "en", "de");
        System.out.println("German Translation: " + translatedReport);
      } catch (TranslationException e) {
        System.out.println(
            "Translation failed (this is expected if dependencies not installed): "
                + e.getMessage());
      }

      System.out.println("\nTo enable full translation functionality:");
      System.out.println("1. Install Python dependencies: pip install -r requirements.txt");
      System.out.println("2. Run the test: python translation_cli.py --test");

    } catch (Exception e) {
      System.err.println("Error testing translation service: " + e.getMessage());
      e.printStackTrace();
    }
  }
}
