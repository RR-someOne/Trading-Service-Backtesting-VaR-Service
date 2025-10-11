# Import alias for Summarization Models with spaces
from importlib.machinery import SourceFileLoader
from pathlib import Path

_BASE = Path(__file__).resolve().parent
_SPACE_DIR = Path(__file__).resolve().parents[1] / 'src' / 'main' / 'java' / 'com' / 'trading' / 'service' / 'model' / 'Summarization Models'

# Expose pipeline
_pipeline = SourceFileLoader('sum_pipeline', str(_SPACE_DIR / 'pipeline.py')).load_module()  # type: ignore
SummarizationPipeline = _pipeline.SummarizationPipeline  # type: ignore

# Optionally expose classes
_bart = SourceFileLoader('sum_bart', str(_SPACE_DIR / 'bart_model.py')).load_module()  # type: ignore
BartSummarizer = _bart.BartSummarizer  # type: ignore

_t5 = SourceFileLoader('sum_t5', str(_SPACE_DIR / 't5_model.py')).load_module()  # type: ignore
T5Summarizer = _t5.T5Summarizer  # type: ignore
