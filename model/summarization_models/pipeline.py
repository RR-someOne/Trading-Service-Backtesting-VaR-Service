# Thin wrapper to allow `from model.summarization_models.pipeline import SummarizationPipeline`
from importlib.machinery import SourceFileLoader
from pathlib import Path

_SPACE_DIR = Path(__file__).resolve().parents[1] / 'src' / 'main' / 'java' / 'com' / 'trading' / 'service' / 'model' / 'Summarization Models'
SummarizationPipeline = SourceFileLoader('sum_pipeline', str(_SPACE_DIR / 'pipeline.py')).load_module().SummarizationPipeline  # type: ignore
