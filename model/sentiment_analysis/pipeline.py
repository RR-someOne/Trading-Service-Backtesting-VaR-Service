# Thin wrapper to enable `from model.sentiment_analysis.pipeline import SentimentPipeline`
from importlib.machinery import SourceFileLoader
from pathlib import Path

_SPACE_DIR = Path(__file__).resolve().parent.parent / 'Sentiment Analysis Models'
SentimentPipeline = SourceFileLoader('sa_pipeline', str(_SPACE_DIR / 'pipeline.py')).load_module().SentimentPipeline  # type: ignore
