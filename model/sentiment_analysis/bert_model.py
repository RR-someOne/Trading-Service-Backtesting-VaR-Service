from importlib.machinery import SourceFileLoader
from pathlib import Path

_SPACE_DIR = Path(__file__).resolve().parent.parent / 'Sentiment Analysis Models'
mod = SourceFileLoader('sa_bert', str(_SPACE_DIR / 'bert_model.py')).load_module()  # type: ignore
BertSentiment = mod.BertSentiment  # type: ignore
SentimentOutput = mod.SentimentOutput  # type: ignore
