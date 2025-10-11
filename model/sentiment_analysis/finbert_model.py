from importlib.machinery import SourceFileLoader
from pathlib import Path

_SPACE_DIR = Path(__file__).resolve().parent.parent / 'Sentiment Analysis Models'
mod = SourceFileLoader('sa_finbert', str(_SPACE_DIR / 'finbert_model.py')).load_module()  # type: ignore
FinBertSentiment = mod.FinBertSentiment  # type: ignore
SentimentOutput = mod.SentimentOutput  # type: ignore
