"""
Configuration and Requirements for M2M100 Translation Model
Trading Service Backtesting VaR Service Integration
"""

# Required Python packages
REQUIRED_PACKAGES = [
    "torch>=1.9.0",
    "transformers>=4.20.0",
    "tokenizers>=0.12.0",
    "numpy>=1.21.0",
    "torch-audio>=0.9.0",
    "sentencepiece>=0.1.96",
    "sacremoses>=0.0.43",
    "protobuf>=3.17.0"
]

# Model configurations
MODEL_CONFIGS = {
    "small": {
        "model_name": "facebook/m2m100_418M",
        "max_length": 512,
        "memory_usage": "~2GB",
        "speed": "fast",
        "quality": "good"
    },
    "large": {
        "model_name": "facebook/m2m100_1.2B",
        "max_length": 1024,
        "memory_usage": "~6GB",
        "speed": "medium",
        "quality": "excellent"
    }
}

# Language mappings for M2M100
LANGUAGE_MAPPINGS = {
    "english": "en",
    "spanish": "es", 
    "french": "fr",
    "german": "de",
    "italian": "it",
    "portuguese": "pt",
    "russian": "ru",
    "chinese": "zh",
    "japanese": "ja",
    "korean": "ko",
    "arabic": "ar",
    "hindi": "hi",
    "dutch": "nl",
    "swedish": "sv",
    "norwegian": "no",
    "danish": "da",
    "finnish": "fi",
    "polish": "pl",
    "czech": "cs",
    "hungarian": "hu"
}

# Trading domain specific configurations
TRADING_TRANSLATION_CONFIG = {
    "preserve_numbers": True,
    "preserve_trading_terms": True,
    "max_batch_size": 8,
    "confidence_threshold": 0.7,
    "priority_languages": ["en", "es", "fr", "de", "zh", "ja"],
    "cache_translations": True,
    "cache_size": 1000
}

# Performance optimization settings
PERFORMANCE_CONFIG = {
    "use_gpu": True,
    "mixed_precision": True,
    "batch_processing": True,
    "model_caching": True,
    "parallel_processing": False  # Set to True for multi-GPU setups
}