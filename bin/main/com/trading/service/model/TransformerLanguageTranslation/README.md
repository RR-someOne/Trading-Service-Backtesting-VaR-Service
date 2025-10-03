# M2M100 Multi-Language Translation for Trading Service

## Overview

This module provides Multi-Language Translation capabilities using Facebook's M2M100 (Many-to-Many 100 languages) transformer model, specifically designed for trading and financial content translation.

## Features

- **100+ Language Support**: Translate between 100+ languages
- **Trading-Specific**: Preserves trading terminology, currency pairs, and numerical values
- **Context-Aware**: Specialized translation for trading signals, market analysis, and risk reports
- **Java Integration**: Seamless integration with the Trading Service codebase
- **High Performance**: Optimized for real-time trading environments
- **Batch Processing**: Support for multiple translations simultaneously

## Supported Languages

Primary trading languages supported:
- English (en), Spanish (es), French (fr), German (de), Italian (it)
- Portuguese (pt), Russian (ru), Chinese (zh), Japanese (ja), Korean (ko)
- Arabic (ar), Hindi (hi), Dutch (nl), Swedish (sv), Norwegian (no)
- Danish (da), Finnish (fi), Polish (pl), Czech (cs), Hungarian (hu)

## Installation

### Prerequisites

1. **Python 3.8+** with pip
2. **PyTorch** (CPU or GPU version)
3. **CUDA** (optional, for GPU acceleration)

### Step 1: Install Python Dependencies

```bash
cd src/main/java/com/trading/service/model/TransformerLanguageTranslation/
pip install -r requirements.txt
```

### Step 2: Install PyTorch (choose based on your system)

**CPU Version:**
```bash
pip install torch torchvision torchaudio --extra-index-url https://download.pytorch.org/whl/cpu
```

**GPU Version (CUDA 11.6):**
```bash
pip install torch torchvision torchaudio --extra-index-url https://download.pytorch.org/whl/cu116
```

### Step 3: Download M2M100 Model (First Run)

The model will be automatically downloaded on first use (~1.6GB for the 418M model).

## Usage

### Python Direct Usage

```python
from language_translation_models import TradingTranslationService

# Initialize service
service = TradingTranslationService()

# Translate trading signal
signal = "BUY EURUSD at 1.1850, Stop Loss: 1.1800, Take Profit: 1.1950"
translated = service.translate_trading_signal(signal, "en", "es")

# Translate market analysis
analysis = "The EUR/USD pair shows strong bullish momentum with RSI above 70."
result = service.translate_market_analysis(analysis, "en", "fr")

# Translate risk report
risk_report = "Portfolio VaR at 95% confidence level: $125,000."
report_result = service.translate_risk_report(risk_report, "en", "de")
```

### Command Line Interface

```bash
# Test the installation
python translation_cli.py --test

# Translate text directly
python translation_cli.py --text "BUY EURUSD at 1.1850" --source-lang en --target-lang es --context trading_signal

# Using JSON input (for Java integration)
python translation_cli.py --json-input '{"text":"Market analysis text","source_language":"en","target_language":"fr","context":"market_analysis"}'
```

### Java Integration

```java
import com.trading.service.model.TranslationService;

// Initialize service
TranslationService translator = new TranslationService();

// Translate trading signal
String signal = "BUY EURUSD at 1.1850, Stop Loss: 1.1800";
String translated = translator.translateTradingSignal(signal, "en", "es");

// Translate market analysis with details
TranslationService.TranslationResponse response = 
    translator.translateMarketAnalysis("Market analysis text", "en", "fr");

// Batch translation
List<TranslationService.TranslationRequest> requests = Arrays.asList(
    new TranslationService.TranslationRequest.Builder()
        .text("Trading text 1")
        .sourceLanguage("en")
        .targetLanguage("es")
        .context("trading_signal")
        .build()
);

CompletableFuture<List<TranslationService.TranslationResponse>> results = 
    translator.batchTranslate(requests);
```

## Configuration

### Model Selection

Choose between different M2M100 model sizes:

```python
# Small model (418M parameters, ~2GB RAM)
service = TradingTranslationService("facebook/m2m100_418M")

# Large model (1.2B parameters, ~6GB RAM) - Better quality
service = TradingTranslationService("facebook/m2m100_1.2B")
```

### Trading-Specific Settings

Configure translation behavior for trading content:

```python
from translation_config import TRADING_TRANSLATION_CONFIG

# Customize settings
TRADING_TRANSLATION_CONFIG.update({
    "preserve_numbers": True,        # Preserve numerical values
    "preserve_trading_terms": True,  # Preserve trading terminology
    "confidence_threshold": 0.8,     # Minimum confidence score
    "cache_translations": True       # Enable caching
})
```

## Performance Optimization

### GPU Acceleration

For faster translation, use GPU acceleration:

```python
# Check GPU availability
import torch
if torch.cuda.is_available():
    print("GPU acceleration available")
    
# The model will automatically use GPU if available
service = TradingTranslationService("facebook/m2m100_418M")
```

### Batch Processing

Process multiple translations efficiently:

```python
requests = [
    TranslationRequest(text="Text 1", source_language="en", target_language="es"),
    TranslationRequest(text="Text 2", source_language="en", target_language="fr"),
    TranslationRequest(text="Text 3", source_language="en", target_language="de")
]

responses = service.model.batch_translate(requests)
```

## Trading Context Types

The system supports specialized translation for different trading contexts:

1. **trading_signal**: Buy/sell signals with preserved numerical values
2. **market_analysis**: Technical and fundamental analysis
3. **risk_report**: VaR reports and risk assessments
4. **news**: Financial news and updates
5. **research**: Market research and reports
6. **alert**: Trading alerts and notifications

## Error Handling

```python
try:
    result = service.translate_trading_signal(signal, "en", "invalid_lang")
except Exception as e:
    print(f"Translation failed: {e}")
```

## Monitoring and Quality Assessment

```python
from trading_translation_utils import TranslationQualityAssessment

assessor = TranslationQualityAssessment()
quality = assessor.assess_translation_quality(
    original_text, translated_text, "en", "es"
)

print(f"Quality Score: {quality['quality_score']:.2f}")
print(f"Terms Preserved: {quality['preservation_score']:.2f}")
```

## Troubleshooting

### Common Issues

1. **OutOfMemoryError**: Use smaller model or reduce batch size
2. **Model Download Failed**: Check internet connection and retry
3. **CUDA Error**: Ensure compatible CUDA version installed
4. **Import Error**: Verify all dependencies installed correctly

### Performance Tips

1. Use GPU acceleration for faster translation
2. Enable caching for repeated translations
3. Use batch processing for multiple texts
4. Choose appropriate model size for your hardware

## Integration with Trading Service

The translation service integrates seamlessly with the existing Trading Service:

- **Signal Translation**: Translate trading signals for global markets
- **Analysis Translation**: Multi-language market analysis reports
- **Risk Reports**: Translate VaR and risk assessments
- **Client Communication**: Multi-language client notifications

## License

This module uses the M2M100 model from Facebook AI Research, which is licensed under the MIT License.

## Support

For issues and questions:
1. Check the troubleshooting section
2. Review Python and PyTorch compatibility
3. Ensure all dependencies are correctly installed
4. Test with the provided CLI tool first