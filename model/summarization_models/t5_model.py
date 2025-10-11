from importlib.machinery import SourceFileLoader
from pathlib import Path

_SPACE_DIR = Path(__file__).resolve().parents[1] / 'src' / 'main' / 'java' / 'com' / 'trading' / 'service' / 'model' / 'Summarization Models'
mod = SourceFileLoader('sum_t5', str(_SPACE_DIR / 't5_model.py')).load_module()  # type: ignore
T5Summarizer = mod.T5Summarizer  # type: ignore
SummaryOutput = mod.SummaryOutput  # type: ignore
