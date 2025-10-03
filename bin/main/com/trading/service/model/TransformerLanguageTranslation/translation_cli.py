#!/usr/bin/env python3
"""
CLI wrapper for M2M100 Translation Model
Provides command-line interface for Java integration
"""

import sys
import json
import argparse
from pathlib import Path

# Add the current directory to path to import our modules
sys.path.append(str(Path(__file__).parent))

try:
    from language_translation_models import (
        TradingTranslationService, 
        TranslationModelInterface,
        TranslationRequest
    )
except ImportError as e:
    print(json.dumps({"error": f"Failed to import translation modules: {str(e)}"}))
    sys.exit(1)


def main():
    parser = argparse.ArgumentParser(description='M2M100 Translation CLI for Trading Service')
    parser.add_argument('--json-input', type=str, help='JSON input for translation')
    parser.add_argument('--text', type=str, help='Text to translate')
    parser.add_argument('--source-lang', type=str, default='en', help='Source language')
    parser.add_argument('--target-lang', type=str, default='es', help='Target language')
    parser.add_argument('--context', type=str, default='general', help='Translation context')
    parser.add_argument('--model', type=str, default='facebook/m2m100_418M', help='Model name')
    parser.add_argument('--test', action='store_true', help='Run test translation')
    
    args = parser.parse_args()
    
    try:
        if args.json_input:
            # Handle JSON input from Java
            interface = TranslationModelInterface()
            result = interface.translate_json(args.json_input)
            print(result)
            
        elif args.text:
            # Handle direct text input
            service = TradingTranslationService(args.model)
            
            if args.context == 'trading_signal':
                result = service.translate_trading_signal(
                    args.text, args.source_lang, args.target_lang
                )
                output = {"translated_text": result}
            elif args.context == 'market_analysis':
                result = service.translate_market_analysis(
                    args.text, args.source_lang, args.target_lang
                )
                output = result
            elif args.context == 'risk_report':
                result = service.translate_risk_report(
                    args.text, args.source_lang, args.target_lang
                )
                output = result
            else:
                # General translation
                request = TranslationRequest(
                    text=args.text,
                    source_language=args.source_lang,
                    target_language=args.target_lang
                )
                response = service.model.translate_text(request)
                output = {
                    "translated_text": response.translated_text,
                    "confidence": response.confidence_score,
                    "processing_time": response.processing_time
                }
            
            print(json.dumps(output))
            
        elif args.test:
            # Run test translation
            run_test_translation(args.model)
            
        else:
            parser.print_help()
            
    except Exception as e:
        error_output = {"error": str(e)}
        print(json.dumps(error_output))
        sys.exit(1)


def run_test_translation(model_name):
    """Run test translations to verify model functionality"""
    print("Testing M2M100 Translation Model...")
    
    try:
        service = TradingTranslationService(model_name)
        
        # Test 1: Trading Signal
        signal = "BUY EURUSD at 1.1850, Stop Loss: 1.1800, Take Profit: 1.1950"
        result1 = service.translate_trading_signal(signal, "en", "es")
        
        # Test 2: Market Analysis
        analysis = "The EUR/USD pair shows strong bullish momentum with RSI above 70."
        result2 = service.translate_market_analysis(analysis, "en", "fr")
        
        # Test 3: Risk Report
        risk_report = "Portfolio VaR at 95% confidence level: $125,000."
        result3 = service.translate_risk_report(risk_report, "en", "de")
        
        test_results = {
            "status": "success",
            "model": model_name,
            "tests": {
                "trading_signal": {
                    "original": signal,
                    "translated": result1,
                    "target_language": "es"
                },
                "market_analysis": {
                    "original": analysis,
                    "translated": result2["translated_text"],
                    "confidence": result2["confidence"],
                    "target_language": "fr"
                },
                "risk_report": {
                    "original": risk_report,
                    "translated": result3["translated"],
                    "confidence": result3["confidence"],
                    "target_language": "de"
                }
            },
            "supported_languages": service.get_supported_languages()
        }
        
        print(json.dumps(test_results, indent=2))
        
    except Exception as e:
        error_results = {
            "status": "error",
            "message": str(e),
            "model": model_name
        }
        print(json.dumps(error_results, indent=2))


if __name__ == "__main__":
    main()