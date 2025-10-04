# Trading-Service-Backtesting-VaR-service-

## README Content Structure

This README is organized to help contributors and maintainers quickly find the project's purpose, architecture, and how to run or extend the service.

- Overview: short project summary and design goals
- High-level components: description of core subsystems
- Suggested tech stack: recommended tools, languages and libraries
- Project layout: opinionated directory structure and key files
- Getting started: build, run, test, and Docker instructions (TBD)
- Development notes: coding standards, testing, CI, and deployment (TBD)

## Overview

Real-time trading engine that can attach strategies (AI models or rule-based).

Offline backtesting engine to validate strategies on historical data.

VaR service to estimate portfolio risk (historical, parametric, Monte Carlo).

Safe separation between simulated/backtest logic and live execution.

Observable, testable, and deployable (Docker + CI).

## High-level components

- MarketData ingestion (streaming & historical store)
- Strategy (AI model or rule-based)
- TradingEngine (live execution loop, risk checks)
- OrderService (broker adapter / mock)
- Backtester (historical replay & performance metrics)
- VaRService (historical, parametric, Monte Carlo)
- Persistence (Postgres for orders/logs, time-series DB optional)
- API (REST/gRPC for status and control)
- Monitoring (Prometheus/Grafana, logs)

## Neural Translation System

### Overview

The trading service includes a sophisticated multi-language translation system powered by Facebook's M2M100 transformer model, capable of translating trading content across 100+ languages while preserving financial terminology and context.

### Key Features

- **Multi-Language Support**: Translate between 100+ languages using state-of-the-art M2M100 transformer
- **Trading-Specific Terminology**: Preserves financial and trading terms during translation
- **Batch Processing**: Efficient handling of multiple translation requests
- **Graceful Fallback**: Works with or without ML dependencies installed
- **Java Integration**: Seamless integration with existing Java codebase
- **Caching Support**: Optional translation caching for improved performance

### Architecture

The translation system consists of several components:

1. **Python ML Backend** (`language_translation_models.py`)
   - Core M2M100 model implementation
   - Trading terminology preservation
   - Batch processing capabilities
   - PyTorch and Hugging Face integration

2. **Java Integration Layer** 
   - `SimpleTranslationService.java` - Lightweight integration (no external dependencies)
   - `TranslationService.java` - Full-featured integration with caching and batch support

3. **CLI Interface** (`translation_cli.py`)
   - Command-line interface for Java-Python communication
   - JSON-based request/response handling

4. **Configuration & Utilities**
   - `translation_config.py` - Configuration management
   - `translation_utils.py` - Helper functions and utilities
   - `requirements.txt` - Python dependencies

### Quick Start

#### Option 1: Simple Integration (No ML Dependencies Required)

```java
import com.trading.service.model.TransformerLanguageTranslation.SimpleTranslationService;

SimpleTranslationService translator = new SimpleTranslationService();
String result = translator.translateText("Buy 100 shares of AAPL", "en", "es");
System.out.println(result); // Returns translated text or graceful fallback
```

#### Option 2: Full ML-Powered Translation

1. Install Python dependencies:
```bash
pip install -r src/main/java/com/trading/service/model/TransformerLanguageTranslation/requirements.txt
```

2. Use the full-featured service:
```java
import com.trading.service.model.TransformerLanguageTranslation.TranslationService;

TranslationService translator = new TranslationService();
TranslationResponse response = translator.translateText("Market volatility increased", "en", "fr");
System.out.println(response.getTranslatedText());
```

### Supported Languages

The M2M100 model supports translation between major languages including:
- English (en), Spanish (es), French (fr), German (de)
- Chinese (zh), Japanese (ja), Korean (ko)
- Arabic (ar), Russian (ru), Portuguese (pt)
- Italian (it), Dutch (nl), Polish (pl)
- And 90+ additional languages

### Configuration

Translation behavior can be customized through `translation_config.py`:

```python
TRANSLATION_CONFIG = {
    "model_name": "facebook/m2m100_418M",  # Model size variant
    "max_length": 512,                    # Maximum translation length
    "num_beams": 5,                       # Beam search parameter
    "preserve_trading_terms": True,       # Preserve financial terminology
    "batch_size": 10,                     # Batch processing size
    "cache_translations": True            # Enable translation caching
}
```

### Trading Terminology Preservation

The system includes specialized logic to preserve trading-specific terms:

- **Financial Instruments**: AAPL, MSFT, BTC, ETH, USD, EUR
- **Trading Actions**: Buy, Sell, Hold, Long, Short
- **Market Terms**: Volatility, Liquidity, Support, Resistance
- **Technical Indicators**: RSI, MACD, Bollinger Bands

### Testing

The translation system includes comprehensive tests:

```bash
# Run all tests including translation components
./gradlew test

# Test specific translation functionality
./gradlew test --tests "*Translation*"
```

### Performance Considerations

- **Model Loading**: Initial model load takes 10-30 seconds depending on hardware
- **Translation Speed**: ~1-5 seconds per sentence depending on length and complexity
- **Memory Usage**: Requires ~2-4GB RAM for M2M100 model in memory
- **Caching**: Enables sub-second response times for repeated translations

### Troubleshooting

**Common Issues:**

1. **Python Dependencies Missing**: The system gracefully falls back to placeholder text
2. **Out of Memory**: Use smaller model variant (e.g., `facebook/m2m100_418M`)
3. **Slow Performance**: Enable caching and consider batch processing
4. **Language Not Supported**: Check M2M100 language codes in documentation

**Debug Mode:**

Enable detailed logging by setting environment variable:
```bash
export TRANSLATION_DEBUG=true
```

### Files and Structure

```
src/main/java/com/trading/service/model/TransformerLanguageTranslation/
├── SimpleTranslationService.java     # Lightweight Java integration
├── TranslationService.java           # Full-featured Java integration
├── language_translation_models.py    # Core M2M100 implementation
├── translation_cli.py               # Python CLI interface
├── translation_config.py            # Configuration settings
├── translation_utils.py             # Utility functions
├── requirements.txt                 # Python dependencies
├── README_Translation.md            # Detailed documentation
└── example_usage.py                # Usage examples
```

## Backtesting System with VaR Integration

### Overview

The trading service includes a comprehensive backtesting system that implements the complete workflow for historical strategy evaluation with integrated Value-at-Risk (VaR) analysis. The system supports multi-asset backtesting with realistic order execution simulation and rolling VaR calculations.

### Core Workflow

The backtesting system implements the following complete workflow:

1. **Load Historical Price Series**: Load OHLC data for multiple assets (A, B, etc.) from CSV files or generate synthetic data
2. **Calculate Daily Returns**: Compute both simple and log returns for statistical analysis
3. **Strategy Execution**: Feed OHLC bars to trading strategies and generate realistic order fills with slippage
4. **Portfolio Management**: Track daily P&L, update portfolio positions, and maintain cash balances
5. **Rolling VaR Analysis**: Calculate VaR using last N returns with configurable rolling window
6. **Comprehensive Reporting**: Generate detailed backtest reports including VaR metrics and performance statistics

### Key Components

#### Data Models

**OHLC (Open, High, Low, Close)**
```java
// Represents price data for a specific time period
OHLC ohlc = new OHLC("AAPL", LocalDate.of(2023, 1, 1), 150.0, 152.5, 149.0, 151.0, 1000000);
double dailyReturn = ohlc.getDailyReturn(previousClose);
double logReturn = ohlc.getLogReturn(previousClose);
```

**Portfolio Management**
```java
// Initialize portfolio with cash
Portfolio portfolio = new Portfolio(100000.0); // $100,000

// Execute orders
Order buyOrder = new Order("BUY1", "AAPL", Order.OrderType.BUY, 100, 150.0, LocalDate.now());
buyOrder.fill(100, 150.50, LocalDate.now()); // Realistic fill with slippage
portfolio.executeOrder(buyOrder);

// Calculate portfolio value
Map<String, Double> currentPrices = Map.of("AAPL", 155.0);
double portfolioValue = portfolio.getPortfolioValue(currentPrices);
```

#### Market Data Loading

**MarketDataLoader**
```java
MarketDataLoader loader = new MarketDataLoader();

// Load from CSV file (format: Date,Open,High,Low,Close,Volume)
List<OHLC> historicalData = loader.loadHistoricalData("AAPL", "path/to/aapl.csv");

// Generate synthetic data for testing
List<OHLC> sampleData = loader.generateSampleData("TEST", LocalDate.now().minusDays(100), 100, 100.0);

// Calculate returns
List<Double> dailyReturns = loader.calculateDailyReturns(historicalData);
List<Double> logReturns = loader.calculateLogReturns(historicalData);

// Filter by date range
List<OHLC> filtered = loader.filterByDateRange(historicalData, startDate, endDate);
```

#### Trading Strategies

**TradingStrategy Interface**
```java
public interface TradingStrategy {
    void initialize(List<OHLC> historicalData);
    List<Order> generateSignals(OHLC currentBar, List<OHLC> historicalBars, LocalDate currentDate);
    String getStrategyName();
    void cleanup();
}
```

**Moving Average Crossover Strategy**
```java
// Create strategy: 10-day MA crosses 30-day MA, 100 shares per trade
TradingStrategy strategy = new MovingAverageCrossoverStrategy(10, 30, 100);

// Strategy generates buy/sell signals based on MA crossovers
List<Order> signals = strategy.generateSignals(currentBar, historicalBars, currentDate);
```

#### VaR Integration

**VaRService with Multiple Methods**
```java
VaRService varService = new VaRService();
double portfolioValue = 100000.0;
double confidenceLevel = 0.95; // 95% confidence
List<Double> returns = portfolioReturns.subList(portfolioReturns.size() - 30, portfolioReturns.size()); // 30-day window

// Calculate VaR using different methods
double historicalVaR = VaRService.historicalVaR(returns, confidenceLevel, portfolioValue);
double parametricVaR = VaRService.parametricVaR(returns, confidenceLevel, portfolioValue);
double monteCarloVaR = VaRService.monteCarloVaR(returns, confidenceLevel, portfolioValue, 10000);
```

**VaRReport for Daily Tracking**
```java
VaRReport dailyVaR = new VaRReport(
    LocalDate.now(),
    portfolioValue,
    historicalVaR,
    parametricVaR, 
    monteCarloVaR,
    confidenceLevel,
    windowSize
);

// Get VaR as percentage of portfolio
double historicalVaRPercent = dailyVaR.getHistoricalVaRPercent(); // e.g., 2.5%
```

### Backtesting Configuration and Execution

**BacktestConfig Setup**
```java
// Configure backtest parameters
BacktestConfig config = new BacktestConfig(
    "Multi-Asset MA Crossover with VaR",           // Description
    Arrays.asList("AAPL", "MSFT"),                 // Asset symbols
    Map.of("AAPL", "data/aapl.csv", "MSFT", "data/msft.csv"), // Data file paths
    LocalDate.of(2023, 1, 1),                     // Start date
    LocalDate.of(2023, 12, 31),                   // End date
    100000.0,                                      // Initial cash
    new MovingAverageCrossoverStrategy(10, 30, 100), // Trading strategy
    30,                                            // VaR rolling window (days)
    0.95                                           // VaR confidence level
);
```

**Running the Backtest**
```java
Backtest backtest = new Backtest();
BacktestResult result = backtest.run(config);

// Access comprehensive results
System.out.println("Total Return: " + result.getTotalReturn() + "%");
System.out.println("Max Drawdown: " + result.getMaxDrawdown() + "%");
System.out.println("Sharpe Ratio: " + result.getSharpeRatio());

// Access VaR metrics
System.out.println("Average Historical VaR: $" + result.getAverageHistoricalVaR());
System.out.println("Average Parametric VaR: $" + result.getAverageParametricVaR());
System.out.println("Average Monte Carlo VaR: $" + result.getAverageMonteCarloVaR());

// Daily VaR reports
List<VaRReport> dailyVaRReports = result.getDailyVaRReports();
for (VaRReport report : dailyVaRReports) {
    System.out.println(report); // Detailed daily VaR information
}
```

### Advanced Features

#### Realistic Order Execution
- **Slippage Simulation**: Orders filled with configurable slippage (default 0.1%)
- **Price Validation**: Fill prices constrained within daily high/low range
- **Market Impact**: Simulated market impact for large orders

#### Rolling VaR Analysis
- **Configurable Window**: 30-day default, customizable rolling window
- **Multiple Methods**: Historical, Parametric (normal distribution), Monte Carlo
- **Daily Tracking**: VaR calculated and stored for each trading day
- **Percentage Reporting**: VaR expressed both in absolute dollars and as portfolio percentage

#### Performance Metrics
- **Total Return**: (Final Value - Initial Value) / Initial Value
- **Maximum Drawdown**: Largest peak-to-trough decline
- **Sharpe Ratio**: Risk-adjusted return metric (assuming 0% risk-free rate)
- **Volatility**: Annualized portfolio volatility

### File Structure

```
src/main/java/com/trading/service/
├── backtest/
│   ├── Backtest.java                    # Main backtesting engine
│   ├── BacktestConfig.java              # Configuration parameters
│   ├── BacktestResult.java              # Comprehensive results
│   ├── VaRReport.java                   # Daily VaR reporting
│   └── BacktestExample.java             # Usage examples
├── backtesting/
│   └── MarketDataLoader.java            # Historical data loading
├── model/
│   ├── OHLC.java                        # Price data model
│   ├── Order.java                       # Order management
│   └── Portfolio.java                   # Portfolio tracking
├── strategy/
│   ├── TradingStrategy.java             # Strategy interface
│   └── MovingAverageCrossoverStrategy.java # MA crossover implementation
└── risk/
    └── VaRService.java                  # VaR calculations
```

### Testing and Validation

Run the backtesting demonstration:
```bash
# Python demonstration showing complete workflow
python3 backtesting_demo.py

# Java unit tests (when compilation issues resolved)
./gradlew test --tests "*Backtest*"
```

### Sample Output

```
=== BACKTEST RESULTS SUMMARY ===
Status: SUCCESS
Initial Value: $100,000.00
Final Value: $100,673.46
Total Return: 0.67%
Max Drawdown: 2.22%
Sharpe Ratio: 0.296
Trading Days: 252
VaR Reports Generated: 223

=== AVERAGE VAR METRICS ===
Average Historical VaR: $199.37
Average Parametric VaR: $181.18

=== SAMPLE VAR REPORTS ===
Date: 2023-09-09
  Portfolio Value: $100,673.46
  Historical VaR: $237.14 (0.24%)
  Parametric VaR: $257.41 (0.26%)
  Monte Carlo VaR: $245.68 (0.24%)
```

### Integration with Existing Components

The backtesting system seamlessly integrates with:
- **VaRService**: Existing risk management for VaR calculations
- **ONNX Models**: Strategies can use ML models for signal generation
- **Translation System**: Multi-language reporting and documentation
- **Existing Architecture**: Follows established patterns and interfaces

## Suggested tech stack

- Java 17 (mandatory)
- Build: Gradle (wrapper enabled)
- Web/API: Spring Boot (optional) or lightweight framework (Micronaut/Quarkus)
- Data: Postgres for relational; local file storage for CSV historical data; optional TimescaleDB
- Messaging: Kafka or Redis Streams (optional) for live tick/event ingestion
- ML/AI: ONNX runtime (for exported models), TensorFlow Java, or call out to Python microservice (recommended for heavy training)
- Math & stats: Apache Commons Math, Smile, or ojAlgo
- Serialization: Jackson
- Testing: JUnit 5, Mockito
- Logging: SLF4J + Logback
- Container: Docker
- CI: GitHub Actions / GitLab CI
- Metrics: Micrometer + Prometheus + Grafana

<img width="600" height="367" alt="Screenshot 2025-09-30 at 10 39 07 AM" src="https://github.com/user-attachments/assets/1976f16c-58fd-4961-8128-f0b50b98619b" />


## Getting started

Quick steps to build, run and test the project locally. Adjust the commands if your project uses Spring Boot / different jar name.

Build the project with Gradle:

```bash
./gradlew clean build
```

Run the application (if packaged as a fat/boot jar):

```bash
java -jar build/libs/<your-app>.jar
```

Alternatively (if using Spring Boot plugin):

```bash
./gradlew bootRun
```

Run tests:

```bash
./gradlew test
```

Docker (build & run):

```bash
# build the project first, then build the image
./gradlew clean build
docker build -t trading-service:latest .
# run the container, mapping port 8080
docker run -p 8080:8080 trading-service:latest
```

Notes:
- Replace `<your-app>.jar` with the actual jar filename produced under `build/libs/`.
- If you want a multi-stage Docker build that compiles inside the image, I can add that variant.

## Development notes (TBD)

- Coding style: follow project conventions
- Tests: unit and integration tests with JUnit 5
- CI: GitHub Actions for build and tests

## Formatting

This project uses Spotless with google-java-format. To format code locally run:

```bash
./gradlew :app:format
```

CI will run `./gradlew spotlessCheck` and fail if formatting is not applied.
