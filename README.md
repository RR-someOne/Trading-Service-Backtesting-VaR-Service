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
