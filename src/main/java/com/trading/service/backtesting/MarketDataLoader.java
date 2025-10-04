package com.trading.service.backtesting;

import com.trading.service.model.OHLC;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Enhanced MarketDataLoader for loading historical price series for backtesting. Supports loading
 * OHLC data from CSV files and calculating returns.
 */
public class MarketDataLoader {

  private static final DateTimeFormatter DEFAULT_DATE_FORMAT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd");

  public MarketDataLoader() {}

  /**
   * Load historical OHLC data from CSV file. Expected CSV format: Date,Open,High,Low,Close,Volume
   *
   * @param symbol Asset symbol (e.g., "AAPL", "MSFT")
   * @param csvFilePath Path to CSV file containing historical data
   * @return List of OHLC bars sorted by date (ascending)
   */
  public List<OHLC> loadHistoricalData(String symbol, String csvFilePath) throws IOException {
    List<OHLC> ohlcData = new ArrayList<>();

    try (BufferedReader reader = new BufferedReader(new FileReader(csvFilePath))) {
      String line;
      boolean isFirstLine = true;

      while ((line = reader.readLine()) != null) {
        // Skip header line
        if (isFirstLine) {
          isFirstLine = false;
          continue;
        }

        String[] parts = line.split(",");
        if (parts.length >= 6) {
          try {
            LocalDate date = LocalDate.parse(parts[0].trim(), DEFAULT_DATE_FORMAT);
            double open = Double.parseDouble(parts[1].trim());
            double high = Double.parseDouble(parts[2].trim());
            double low = Double.parseDouble(parts[3].trim());
            double close = Double.parseDouble(parts[4].trim());
            long volume = Long.parseLong(parts[5].trim());

            ohlcData.add(new OHLC(symbol, date, open, high, low, close, volume));
          } catch (Exception e) {
            System.err.println("Error parsing line: " + line + " - " + e.getMessage());
          }
        }
      }
    }

    // Sort by date to ensure chronological order
    ohlcData.sort(Comparator.comparing(OHLC::getDate));
    return ohlcData;
  }

  /**
   * Generate sample historical data for testing purposes. Creates synthetic OHLC data with
   * realistic price movements.
   *
   * @param symbol Asset symbol
   * @param startDate Start date for data generation
   * @param days Number of days to generate
   * @param startPrice Starting price
   * @return List of synthetic OHLC bars
   */
  public List<OHLC> generateSampleData(
      String symbol, LocalDate startDate, int days, double startPrice) {
    List<OHLC> ohlcData = new ArrayList<>();
    Random random = new Random(42); // Fixed seed for reproducible results
    double currentPrice = startPrice;

    for (int i = 0; i < days; i++) {
      LocalDate date = startDate.plusDays(i);

      // Generate realistic price movements
      double dailyReturn = (random.nextGaussian() * 0.02); // 2% daily volatility
      double open = currentPrice;
      double close = open * (1 + dailyReturn);

      // Generate high and low based on intraday volatility
      double intradayVolatility = Math.abs(random.nextGaussian() * 0.01); // 1% intraday volatility
      double high = Math.max(open, close) * (1 + intradayVolatility);
      double low = Math.min(open, close) * (1 - intradayVolatility);

      // Generate volume (random between 1M and 10M shares)
      long volume = 1_000_000 + random.nextInt(9_000_000);

      ohlcData.add(new OHLC(symbol, date, open, high, low, close, volume));
      currentPrice = close;
    }

    return ohlcData;
  }

  /**
   * Calculate daily returns from OHLC data. Returns are calculated as (close_t - close_t-1) /
   * close_t-1
   *
   * @param ohlcData List of OHLC bars (must be sorted by date)
   * @return List of daily returns (first day will have 0 return)
   */
  public List<Double> calculateDailyReturns(List<OHLC> ohlcData) {
    List<Double> returns = new ArrayList<>();

    if (ohlcData.isEmpty()) {
      return returns;
    }

    // First day has no previous close, so return is 0
    returns.add(0.0);

    for (int i = 1; i < ohlcData.size(); i++) {
      double previousClose = ohlcData.get(i - 1).getClose();
      double currentClose = ohlcData.get(i).getClose();
      double dailyReturn = (currentClose - previousClose) / previousClose;
      returns.add(dailyReturn);
    }

    return returns;
  }

  /**
   * Calculate log returns from OHLC data. Log returns are more suitable for statistical analysis
   * and VaR calculations.
   *
   * @param ohlcData List of OHLC bars (must be sorted by date)
   * @return List of log returns
   */
  public List<Double> calculateLogReturns(List<OHLC> ohlcData) {
    List<Double> logReturns = new ArrayList<>();

    if (ohlcData.isEmpty()) {
      return logReturns;
    }

    // First day has no previous close, so return is 0
    logReturns.add(0.0);

    for (int i = 1; i < ohlcData.size(); i++) {
      double previousClose = ohlcData.get(i - 1).getClose();
      double currentClose = ohlcData.get(i).getClose();

      if (previousClose > 0 && currentClose > 0) {
        double logReturn = Math.log(currentClose / previousClose);
        logReturns.add(logReturn);
      } else {
        logReturns.add(0.0);
      }
    }

    return logReturns;
  }

  /**
   * Load historical data for multiple assets.
   *
   * @param symbolToCsvPath Map of symbol to CSV file path
   * @return Map of symbol to OHLC data
   */
  public Map<String, List<OHLC>> loadMultipleAssets(Map<String, String> symbolToCsvPath) {
    Map<String, List<OHLC>> results = new HashMap<>();

    for (Map.Entry<String, String> entry : symbolToCsvPath.entrySet()) {
      String symbol = entry.getKey();
      String csvPath = entry.getValue();

      try {
        List<OHLC> data = loadHistoricalData(symbol, csvPath);
        results.put(symbol, data);
        System.out.println("Loaded " + data.size() + " records for " + symbol);
      } catch (IOException e) {
        System.err.println("Failed to load data for " + symbol + ": " + e.getMessage());
      }
    }

    return results;
  }

  /**
   * Get price data within a specific date range.
   *
   * @param ohlcData Full OHLC data
   * @param startDate Start date (inclusive)
   * @param endDate End date (inclusive)
   * @return Filtered OHLC data within the date range
   */
  public List<OHLC> filterByDateRange(List<OHLC> ohlcData, LocalDate startDate, LocalDate endDate) {
    return ohlcData.stream()
        .filter(ohlc -> !ohlc.getDate().isBefore(startDate) && !ohlc.getDate().isAfter(endDate))
        .collect(Collectors.toList());
  }

  /**
   * Get the most recent N data points.
   *
   * @param ohlcData Full OHLC data (should be sorted by date)
   * @param n Number of recent data points to return
   * @return Last N OHLC data points
   */
  public List<OHLC> getRecentData(List<OHLC> ohlcData, int n) {
    if (ohlcData.size() <= n) {
      return new ArrayList<>(ohlcData);
    }

    return ohlcData.subList(ohlcData.size() - n, ohlcData.size());
  }
}
