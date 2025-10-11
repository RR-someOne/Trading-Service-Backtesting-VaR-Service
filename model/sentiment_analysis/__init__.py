# Friendly import alias for 'model/Sentiment Analysis Models'
from importlib.machinery import SourceFileLoader
from pathlib import Path

_BASE = Path(__file__).resolve().parent
_SPACE_DIR = _BASE.parent / 'Sentiment Analysis Models'

# Expose SentimentPipeline at package level
_pipeline_path = _SPACE_DIR / 'pipeline.py'
SentimentPipeline = SourceFileLoader('sa_pipeline', str(_pipeline_path)).load_module().SentimentPipeline  # type: ignore
