"""
Trading-specific utilities for M2M100 translation model
Handles specialized trading terminology and financial data preservation
"""

import re
import json
from typing import Dict, List, Tuple, Set
from dataclasses import dataclass
from enum import Enum


class TradingContext(Enum):
    """Trading context types for specialized translation"""
    SIGNAL = "signal"
    ANALYSIS = "analysis"
    RISK_REPORT = "risk_report"
    NEWS = "news"
    RESEARCH = "research"
    ALERT = "alert"
    GENERAL = "general"


@dataclass
class TradingTermPattern:
    """Pattern for trading term recognition"""
    term: str
    pattern: str
    preserve_case: bool = True
    context_dependent: bool = False


class TradingTerminologyExtractor:
    """Extracts and preserves trading-specific terminology"""
    
    def __init__(self):
        self.trading_patterns = self._initialize_trading_patterns()
        self.currency_pairs = self._initialize_currency_pairs()
        self.number_patterns = self._initialize_number_patterns()
    
    def _initialize_trading_patterns(self) -> List[TradingTermPattern]:
        """Initialize trading term patterns"""
        return [
            # Trading actions
            TradingTermPattern("BUY", r"\bBUY\b", True, False),
            TradingTermPattern("SELL", r"\bSELL\b", True, False),
            TradingTermPattern("HOLD", r"\bHOLD\b", True, False),
            TradingTermPattern("LONG", r"\bLONG\b", True, False),
            TradingTermPattern("SHORT", r"\bSHORT\b", True, False),
            
            # Technical indicators
            TradingTermPattern("RSI", r"\bRSI\b", True, False),
            TradingTermPattern("MACD", r"\bMACD\b", True, False),
            TradingTermPattern("SMA", r"\bSMA\b", True, False),
            TradingTermPattern("EMA", r"\bEMA\b", True, False),
            TradingTermPattern("Bollinger Bands", r"\bBollinger\s+Bands\b", True, False),
            
            # Market terms
            TradingTermPattern("Stop Loss", r"\bStop\s+Loss\b", True, False),
            TradingTermPattern("Take Profit", r"\bTake\s+Profit\b", True, False),
            TradingTermPattern("Support", r"\bSupport\b", True, True),
            TradingTermPattern("Resistance", r"\bResistance\b", True, True),
            TradingTermPattern("Breakout", r"\bBreakout\b", True, False),
            
            # Risk terms
            TradingTermPattern("VaR", r"\bVaR\b", True, False),
            TradingTermPattern("Value at Risk", r"\bValue\s+at\s+Risk\b", True, False),
            TradingTermPattern("Expected Shortfall", r"\bExpected\s+Shortfall\b", True, False),
            TradingTermPattern("Sharpe Ratio", r"\bSharpe\s+Ratio\b", True, False),
            
            # Financial instruments
            TradingTermPattern("Option", r"\bOption\b", True, True),
            TradingTermPattern("Future", r"\bFuture\b", True, True),
            TradingTermPattern("Swap", r"\bSwap\b", True, True),
            TradingTermPattern("Bond", r"\bBond\b", True, True),
            TradingTermPattern("Equity", r"\bEquity\b", True, True),
        ]
    
    def _initialize_currency_pairs(self) -> Set[str]:
        """Initialize currency pair patterns"""
        major_pairs = {
            "EURUSD", "GBPUSD", "USDJPY", "USDCHF", "USDCAD", "AUDUSD", "NZDUSD",
            "EURJPY", "GBPJPY", "EURGBP", "EURAUD", "EURCHF", "EURCZK", "EURHUF",
            "GBPAUD", "GBPCAD", "GBPCHF", "AUDCAD", "AUDCHF", "AUDJPY", "AUDNZD",
            "CADCHF", "CADJPY", "CHFJPY", "NZDCAD", "NZDCHF", "NZDJPY"
        }
        
        # Add reverse pairs
        reverse_pairs = set()
        for pair in major_pairs:
            if len(pair) == 6:
                reverse_pair = pair[3:] + pair[:3]
                reverse_pairs.add(reverse_pair)
        
        return major_pairs.union(reverse_pairs)
    
    def _initialize_number_patterns(self) -> List[str]:
        """Initialize number preservation patterns"""
        return [
            r'\b\d+\.\d+\b',  # Decimal numbers
            r'\b\d+,\d+\b',   # Numbers with comma
            r'\b\d+%\b',      # Percentages
            r'\$\d+(?:,\d{3})*(?:\.\d{2})?\b',  # Dollar amounts
            r'€\d+(?:,\d{3})*(?:\.\d{2})?\b',   # Euro amounts
            r'£\d+(?:,\d{3})*(?:\.\d{2})?\b',   # Pound amounts
            r'\b\d+\s*pips?\b',  # Pips
            r'\b\d+\s*lots?\b',  # Lots
            r'\b\d+x\s*leverage\b',  # Leverage
        ]
    
    def extract_trading_terms(self, text: str) -> List[Tuple[str, int, int]]:
        """Extract trading terms with their positions"""
        terms = []
        
        # Extract trading patterns
        for pattern in self.trading_patterns:
            matches = re.finditer(pattern.pattern, text, re.IGNORECASE)
            for match in matches:
                terms.append((match.group(), match.start(), match.end()))
        
        # Extract currency pairs
        currency_pattern = r'\b(?:' + '|'.join(self.currency_pairs) + r')\b'
        matches = re.finditer(currency_pattern, text, re.IGNORECASE)
        for match in matches:
            terms.append((match.group(), match.start(), match.end()))
        
        # Extract numbers
        for pattern in self.number_patterns:
            matches = re.finditer(pattern, text)
            for match in matches:
                terms.append((match.group(), match.start(), match.end()))
        
        # Sort by position and remove overlaps
        terms.sort(key=lambda x: x[1])
        filtered_terms = []
        last_end = -1
        
        for term, start, end in terms:
            if start >= last_end:
                filtered_terms.append((term, start, end))
                last_end = end
        
        return filtered_terms
    
    def preserve_terms_in_translation(self, original: str, translated: str, 
                                    terms: List[Tuple[str, int, int]]) -> str:
        """Preserve important terms in translation"""
        # This is a simplified implementation
        # In production, you'd use more sophisticated alignment techniques
        
        preserved_translation = translated
        
        # For now, just ensure currency pairs and numbers are preserved
        for term, start, end in terms:
            original_term = original[start:end]
            
            # Check if term should be preserved exactly
            if (any(pair in original_term.upper() for pair in self.currency_pairs) or
                re.match(r'.*\d+.*', original_term)):
                
                # Simple replacement (in production, use better alignment)
                if original_term not in preserved_translation:
                    # This is a placeholder - implement proper term preservation
                    pass
        
        return preserved_translation


class TradingDataPreprocessor:
    """Preprocesses trading data for optimal translation"""
    
    def __init__(self):
        self.terminology_extractor = TradingTerminologyExtractor()
    
    def preprocess_text(self, text: str, context: TradingContext) -> Dict[str, any]:
        """Preprocess text based on trading context"""
        
        # Extract terms to preserve
        terms_to_preserve = self.terminology_extractor.extract_trading_terms(text)
        
        # Clean and normalize text
        cleaned_text = self._clean_text(text)
        
        # Add context-specific preprocessing
        if context == TradingContext.SIGNAL:
            cleaned_text = self._preprocess_signal(cleaned_text)
        elif context == TradingContext.RISK_REPORT:
            cleaned_text = self._preprocess_risk_report(cleaned_text)
        elif context == TradingContext.ANALYSIS:
            cleaned_text = self._preprocess_analysis(cleaned_text)
        
        return {
            "preprocessed_text": cleaned_text,
            "terms_to_preserve": terms_to_preserve,
            "original_text": text,
            "context": context.value
        }
    
    def _clean_text(self, text: str) -> str:
        """Basic text cleaning"""
        # Remove extra whitespace
        cleaned = re.sub(r'\s+', ' ', text.strip())
        
        # Normalize quotes
        cleaned = re.sub(r'["""]', '"', cleaned)
        cleaned = re.sub(r"[''']", "'", cleaned)
        
        return cleaned
    
    def _preprocess_signal(self, text: str) -> str:
        """Preprocess trading signals"""
        # Ensure signal format consistency
        text = re.sub(r'\bstop\s*loss\b', 'Stop Loss', text, flags=re.IGNORECASE)
        text = re.sub(r'\btake\s*profit\b', 'Take Profit', text, flags=re.IGNORECASE)
        return text
    
    def _preprocess_risk_report(self, text: str) -> str:
        """Preprocess risk reports"""
        # Standardize risk terminology
        text = re.sub(r'\bvalue\s*at\s*risk\b', 'Value at Risk', text, flags=re.IGNORECASE)
        text = re.sub(r'\bvar\b', 'VaR', text, flags=re.IGNORECASE)
        return text
    
    def _preprocess_analysis(self, text: str) -> str:
        """Preprocess market analysis"""
        # Standardize analysis terminology
        text = re.sub(r'\bbullish\b', 'bullish', text, flags=re.IGNORECASE)
        text = re.sub(r'\bbearish\b', 'bearish', text, flags=re.IGNORECASE)
        return text


class TranslationQualityAssessment:
    """Assesses translation quality for trading content"""
    
    def __init__(self):
        self.terminology_extractor = TradingTerminologyExtractor()
    
    def assess_translation_quality(self, original: str, translated: str, 
                                 source_lang: str, target_lang: str) -> Dict[str, any]:
        """Assess the quality of a translation"""
        
        # Extract terms from both texts
        original_terms = self.terminology_extractor.extract_trading_terms(original)
        translated_terms = self.terminology_extractor.extract_trading_terms(translated)
        
        # Calculate preservation score
        preservation_score = self._calculate_preservation_score(
            original_terms, translated_terms
        )
        
        # Calculate length ratio
        length_ratio = len(translated) / len(original) if len(original) > 0 else 0
        
        # Assess number preservation
        numbers_preserved = self._assess_number_preservation(original, translated)
        
        # Overall quality score (simplified)
        quality_score = (preservation_score + numbers_preserved + 
                        min(1.0, 2.0 - abs(length_ratio - 1.0))) / 3.0
        
        return {
            "quality_score": quality_score,
            "preservation_score": preservation_score,
            "numbers_preserved": numbers_preserved,
            "length_ratio": length_ratio,
            "original_terms_count": len(original_terms),
            "translated_terms_count": len(translated_terms),
            "assessment": "good" if quality_score > 0.8 else "fair" if quality_score > 0.6 else "poor"
        }
    
    def _calculate_preservation_score(self, original_terms: List[Tuple], 
                                    translated_terms: List[Tuple]) -> float:
        """Calculate how well trading terms were preserved"""
        if not original_terms:
            return 1.0
        
        original_term_texts = [term[0].upper() for term in original_terms]
        translated_term_texts = [term[0].upper() for term in translated_terms]
        
        preserved_count = sum(1 for term in original_term_texts 
                            if term in translated_term_texts)
        
        return preserved_count / len(original_terms)
    
    def _assess_number_preservation(self, original: str, translated: str) -> float:
        """Assess how well numbers were preserved"""
        original_numbers = re.findall(r'\d+(?:\.\d+)?', original)
        translated_numbers = re.findall(r'\d+(?:\.\d+)?', translated)
        
        if not original_numbers:
            return 1.0
        
        preserved_count = sum(1 for num in original_numbers 
                            if num in translated_numbers)
        
        return preserved_count / len(original_numbers)


# Utility functions for integration
def load_trading_glossary(file_path: str) -> Dict[str, Dict[str, str]]:
    """Load trading glossary for term consistency"""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            return json.load(f)
    except FileNotFoundError:
        return {}

def save_translation_cache(cache: Dict, file_path: str) -> bool:
    """Save translation cache to file"""
    try:
        with open(file_path, 'w', encoding='utf-8') as f:
            json.dump(cache, f, ensure_ascii=False, indent=2)
        return True
    except Exception:
        return False