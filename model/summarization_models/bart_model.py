from importlib.machinery import SourceFileLoader
from pathlib import Path

_SPACE_DIR = Path(__file__).resolve().parents[1] / 'src' / 'main' / 'java' / 'com' / 'trading' / 'service' / 'model' / 'Summarization Models'
mod = SourceFileLoader('sum_bart', str(_SPACE_DIR / 'bart_model.py')).load_module()  # type: ignore
BartSummarizer = mod.BartSummarizer  # type: ignore
SummaryOutput = mod.SummaryOutput  # type: ignore
