from importlib.machinery import SourceFileLoader
from pathlib import Path

_SPACE_DIR = Path(__file__).resolve().parent.parent / 'Sentiment Analysis Models'
mod = SourceFileLoader('sa_gpt', str(_SPACE_DIR / 'gpt_classifier.py')).load_module()  # type: ignore
GPTClassifier = mod.GPTClassifier  # type: ignore
SentimentOutput = mod.SentimentOutput  # type: ignore
