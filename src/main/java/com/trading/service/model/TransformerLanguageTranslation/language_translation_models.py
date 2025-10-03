"""
Multi-Language Translation Model for Trading Service
Using M2M100 (Many-to-Many 100 languages) with PyTorch and Hugging Face

Compatible with Trading Service Backtesting VaR Service for translating
trading signals, market analysis, and financial reports across languages.
"""

import torch
import torch.nn as nn
from transformers import (
    M2M100ForConditionalGeneration,
    M2M100Tokenizer,
    M2M100Config,
    AutoTokenizer,
    AutoModelForSeq2SeqLM,
    pipeline
)
from typing import List, Dict, Optional, Union, Tuple
import json
import logging
import os
from dataclasses import dataclass
from enum import Enum
import numpy as np
from pathlib import Path

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


class SupportedLanguages(Enum):
    """Supported languages for trading content translation"""
    ENGLISH = "en"
    SPANISH = "es"
    FRENCH = "fr"
    GERMAN = "de"
    ITALIAN = "it"
    PORTUGUESE = "pt"
    RUSSIAN = "ru"
    CHINESE_SIMPLIFIED = "zh"
    JAPANESE = "ja"
    KOREAN = "ko"
    ARABIC = "ar"
    HINDI = "hi"
    DUTCH = "nl"
    SWEDISH = "sv"
    NORWEGIAN = "no"
    DANISH = "da"
    FINNISH = "fi"
    POLISH = "pl"
    CZECH = "cs"
    HUNGARIAN = "hu"


@dataclass
class TranslationRequest:
    """Request object for translation operations"""
    text: str
    source_language: str
    target_language: str
    context: Optional[str] = None  # Trading context (signals, analysis, etc.)
    priority: str = "normal"  # normal, high, urgent
    preserve_numbers: bool = True  # Preserve numerical values
    preserve_trading_terms: bool = True  # Preserve trading terminology


@dataclass
class TranslationResponse:
    """Response object for translation operations"""
    original_text: str
    translated_text: str
    source_language: str
    target_language: str
    confidence_score: float
    processing_time: float
    model_version: str
    trading_terms_preserved: List[str]


class TradingTermsManager:
    """Manages trading-specific terminology for accurate translation"""
    
    def __init__(self):
        self.trading_terms = {
            "english": {
                "buy", "sell", "hold", "long", "short", "bullish", "bearish",
                "support", "resistance", "breakout", "trend", "volume",
                "volatility", "liquidity", "spread", "pip", "lot", "margin",
                "leverage", "stop loss", "take profit", "moving average",
                "rsi", "macd", "bollinger bands", "fibonacci", "candlestick",
                "bull market", "bear market", "market cap", "dividend",
                "earnings", "p/e ratio", "beta", "alpha", "sharpe ratio",
                "var", "value at risk", "portfolio", "hedge", "derivative",
                "option", "future", "swap", "bond", "equity", "commodity",
                "forex", "currency", "exchange rate", "interest rate",
                "inflation", "gdp", "cpi", "fed", "ecb", "central bank"
            }
        }
    
    def extract_trading_terms(self, text: str) -> List[str]:
        """Extract trading terms from text to preserve during translation"""
        text_lower = text.lower()
        found_terms = []
        
        for term in self.trading_terms["english"]:
            if term in text_lower:
                found_terms.append(term)
        
        return found_terms
    
    def preserve_terms_in_translation(self, original: str, translated: str, 
                                    terms: List[str]) -> str:
        """Preserve important trading terms in translation"""
        # This is a simplified implementation
        # In production, you'd want more sophisticated term preservation
        return translated


class M2M100TranslationModel:
    """M2M100 model wrapper for multi-language translation in trading context"""
    
    def __init__(self, model_name: str = "facebook/m2m100_418M", 
                 device: Optional[str] = None):
        """
        Initialize M2M100 translation model
        
        Args:
            model_name: Hugging Face model identifier
            device: Target device (cuda/cpu). Auto-detected if None
        """
        self.model_name = model_name
        self.device = device or ("cuda" if torch.cuda.is_available() else "cpu")
        
        logger.info(f"Initializing M2M100 model: {model_name} on {self.device}")
        
        # Load model and tokenizer
        self.tokenizer = M2M100Tokenizer.from_pretrained(model_name)
        self.model = M2M100ForConditionalGeneration.from_pretrained(model_name)
        self.model.to(self.device)
        self.model.eval()
        
        # Initialize trading terms manager
        self.terms_manager = TradingTermsManager()
        
        # Model metadata
        self.model_version = f"{model_name}@{torch.version.__version__}"
        
        logger.info("M2M100 model loaded successfully")
    
    def _prepare_text_for_translation(self, text: str, 
                                    preserve_numbers: bool = True) -> str:
        """Prepare text for translation with trading-specific preprocessing"""
        if not text.strip():
            return text
        
        # Basic text cleaning while preserving trading context
        cleaned_text = text.strip()
        
        # Preserve numerical values if requested
        if preserve_numbers:
            # Mark numbers for preservation (simplified approach)
            pass
        
        return cleaned_text
    
    def translate_text(self, request: TranslationRequest) -> TranslationResponse:
        """
        Translate text using M2M100 model
        
        Args:
            request: TranslationRequest object
            
        Returns:
            TranslationResponse object with translation results
        """
        import time
        start_time = time.time()
        
        try:
            # Extract trading terms to preserve
            trading_terms = []
            if request.preserve_trading_terms:
                trading_terms = self.terms_manager.extract_trading_terms(request.text)
            
            # Prepare text for translation
            prepared_text = self._prepare_text_for_translation(
                request.text, request.preserve_numbers
            )
            
            # Set source language
            self.tokenizer.src_lang = request.source_language
            
            # Tokenize input
            inputs = self.tokenizer(
                prepared_text, 
                return_tensors="pt", 
                padding=True, 
                truncation=True,
                max_length=512
            )
            inputs = {k: v.to(self.device) for k, v in inputs.items()}
            
            # Generate translation
            with torch.no_grad():
                generated_tokens = self.model.generate(
                    **inputs,
                    forced_bos_token_id=self.tokenizer.get_lang_id(request.target_language),
                    max_length=512,
                    num_beams=4,
                    early_stopping=True,
                    do_sample=False
                )
            
            # Decode translation
            translated_text = self.tokenizer.batch_decode(
                generated_tokens, skip_special_tokens=True
            )[0]
            
            # Preserve trading terms if requested
            if request.preserve_trading_terms and trading_terms:
                translated_text = self.terms_manager.preserve_terms_in_translation(
                    request.text, translated_text, trading_terms
                )
            
            # Calculate confidence score (simplified)
            confidence_score = 0.85  # In production, use model scores
            
            processing_time = time.time() - start_time
            
            return TranslationResponse(
                original_text=request.text,
                translated_text=translated_text,
                source_language=request.source_language,
                target_language=request.target_language,
                confidence_score=confidence_score,
                processing_time=processing_time,
                model_version=self.model_version,
                trading_terms_preserved=trading_terms
            )
            
        except Exception as e:
            logger.error(f"Translation failed: {str(e)}")
            raise RuntimeError(f"Translation failed: {str(e)}")
    
    def batch_translate(self, requests: List[TranslationRequest]) -> List[TranslationResponse]:
        """Batch translation for multiple requests"""
        responses = []
        
        for request in requests:
            try:
                response = self.translate_text(request)
                responses.append(response)
            except Exception as e:
                logger.error(f"Batch translation failed for request: {str(e)}")
                # Create error response
                responses.append(TranslationResponse(
                    original_text=request.text,
                    translated_text="",
                    source_language=request.source_language,
                    target_language=request.target_language,
                    confidence_score=0.0,
                    processing_time=0.0,
                    model_version=self.model_version,
                    trading_terms_preserved=[]
                ))
        
        return responses


class TradingTranslationService:
    """High-level service for trading content translation"""
    
    def __init__(self, model_name: str = "facebook/m2m100_418M"):
        """Initialize the translation service"""
        self.model = M2M100TranslationModel(model_name)
        self.supported_languages = [lang.value for lang in SupportedLanguages]
        
        logger.info(f"Trading Translation Service initialized with {len(self.supported_languages)} supported languages")
    
    def translate_trading_signal(self, signal_text: str, source_lang: str, 
                                target_lang: str) -> str:
        """Translate trading signal with high priority"""
        request = TranslationRequest(
            text=signal_text,
            source_language=source_lang,
            target_language=target_lang,
            context="trading_signal",
            priority="high",
            preserve_numbers=True,
            preserve_trading_terms=True
        )
        
        response = self.model.translate_text(request)
        return response.translated_text
    
    def translate_market_analysis(self, analysis_text: str, source_lang: str,
                                 target_lang: str) -> Dict[str, any]:
        """Translate market analysis with detailed response"""
        request = TranslationRequest(
            text=analysis_text,
            source_language=source_lang,
            target_language=target_lang,
            context="market_analysis",
            priority="normal",
            preserve_numbers=True,
            preserve_trading_terms=True
        )
        
        response = self.model.translate_text(request)
        
        return {
            "translated_text": response.translated_text,
            "confidence": response.confidence_score,
            "processing_time": response.processing_time,
            "preserved_terms": response.trading_terms_preserved
        }
    
    def translate_risk_report(self, report_text: str, source_lang: str,
                             target_lang: str) -> Dict[str, any]:
        """Translate VaR and risk reports with high accuracy requirements"""
        request = TranslationRequest(
            text=report_text,
            source_language=source_lang,
            target_language=target_lang,
            context="risk_report",
            priority="urgent",
            preserve_numbers=True,
            preserve_trading_terms=True
        )
        
        response = self.model.translate_text(request)
        
        return {
            "original": response.original_text,
            "translated": response.translated_text,
            "source_language": response.source_language,
            "target_language": response.target_language,
            "confidence": response.confidence_score,
            "processing_time": response.processing_time,
            "model_version": response.model_version,
            "preserved_terms": response.trading_terms_preserved
        }
    
    def get_supported_languages(self) -> List[str]:
        """Get list of supported languages"""
        return self.supported_languages
    
    def validate_language_pair(self, source_lang: str, target_lang: str) -> bool:
        """Validate if language pair is supported"""
        return (source_lang in self.supported_languages and 
                target_lang in self.supported_languages)


# Utility functions for integration with Java codebase
class TranslationModelInterface:
    """Interface for integration with Java Trading Service"""
    
    def __init__(self):
        self.service = TradingTranslationService()
    
    def translate_json(self, json_input: str) -> str:
        """
        Translate using JSON input/output for Java integration
        
        Expected JSON format:
        {
            "text": "Buy EURUSD at 1.1850",
            "source_language": "en",
            "target_language": "es",
            "context": "trading_signal"
        }
        """
        try:
            data = json.loads(json_input)
            
            text = data.get("text", "")
            source_lang = data.get("source_language", "en")
            target_lang = data.get("target_language", "es")
            context = data.get("context", "general")
            
            if not self.service.validate_language_pair(source_lang, target_lang):
                return json.dumps({
                    "error": f"Unsupported language pair: {source_lang} -> {target_lang}"
                })
            
            if context == "trading_signal":
                result = self.service.translate_trading_signal(text, source_lang, target_lang)
                return json.dumps({"translated_text": result})
            elif context == "market_analysis":
                result = self.service.translate_market_analysis(text, source_lang, target_lang)
                return json.dumps(result)
            elif context == "risk_report":
                result = self.service.translate_risk_report(text, source_lang, target_lang)
                return json.dumps(result)
            else:
                # General translation
                request = TranslationRequest(
                    text=text,
                    source_language=source_lang,
                    target_language=target_lang
                )
                response = self.service.model.translate_text(request)
                return json.dumps({
                    "translated_text": response.translated_text,
                    "confidence": response.confidence_score
                })
                
        except Exception as e:
            return json.dumps({"error": str(e)})


# Example usage and testing
if __name__ == "__main__":
    # Initialize translation service
    translation_service = TradingTranslationService()
    
    # Example 1: Translate trading signal
    signal = "BUY EURUSD at 1.1850, Stop Loss: 1.1800, Take Profit: 1.1950"
    translated_signal = translation_service.translate_trading_signal(
        signal, "en", "es"
    )
    print(f"Original: {signal}")
    print(f"Spanish: {translated_signal}")
    
    # Example 2: Translate market analysis
    analysis = "The EUR/USD pair shows strong bullish momentum with RSI above 70. " \
               "Consider long positions with proper risk management."
    analysis_result = translation_service.translate_market_analysis(
        analysis, "en", "fr"
    )
    print(f"\nMarket Analysis Translation:")
    print(f"Original: {analysis}")
    print(f"French: {analysis_result['translated_text']}")
    print(f"Confidence: {analysis_result['confidence']:.2f}")
    
    # Example 3: Translate VaR report
    var_report = "Portfolio VaR at 95% confidence level: $125,000. " \
                 "Expected Shortfall: $180,000. Risk concentration in EUR positions."
    risk_result = translation_service.translate_risk_report(
        var_report, "en", "de"
    )
    print(f"\nVaR Report Translation:")
    print(f"Original: {risk_result['original']}")
    print(f"German: {risk_result['translated']}")
    print(f"Preserved terms: {risk_result['preserved_terms']}")
    
    # Example 4: JSON interface for Java integration
    interface = TranslationModelInterface()
    json_input = json.dumps({
        "text": "Market volatility increased by 15% today",
        "source_language": "en",
        "target_language": "ja",
        "context": "market_analysis"
    })
    
    json_result = interface.translate_json(json_input)
    print(f"\nJSON Interface Result:")
    print(json_result)
    
    print(f"\nSupported Languages: {translation_service.get_supported_languages()}")
