from pathlib import Path
import importlib.util
import sys

_ROOT = Path(__file__).resolve().parents[2]
_SPACE_DIR = _ROOT / 'src' / 'main' / 'java' / 'com' / 'trading' / 'service' / 'document_processing'

def _load(name: str, file: str):
    spec = importlib.util.spec_from_file_location(f"model.document_processing._{name}", str(_SPACE_DIR / file))
    if spec is None or spec.loader is None:
        raise ImportError(f"Cannot locate {file} in {_SPACE_DIR}")
    mod = importlib.util.module_from_spec(spec)
    sys.modules[spec.name] = mod
    spec.loader.exec_module(mod)  # type: ignore[arg-type]
    return mod

_clean = _load('cleaning', 'cleaning.py')
_parse = _load('parsing', 'parsing.py')
_feat = _load('features', 'features.py')
# Ensure package-qualified names resolve for relative imports
sys.modules['model.document_processing.cleaning'] = _clean
sys.modules['model.document_processing.parsing'] = _parse
sys.modules['model.document_processing.features'] = _feat
_xform = _load('transform', 'transform.py')

# Re-export public API
strip_html = _clean.strip_html
normalize_whitespace = _clean.normalize_whitespace
remove_boilerplate = _clean.remove_boilerplate

Section = _parse.Section
split_paragraphs = _parse.split_paragraphs
detect_sections = _parse.detect_sections
extract_title_and_abstract = _parse.extract_title_and_abstract

DocFeatures = _feat.DocFeatures
compute_features = _feat.compute_features

# Transform API
StructuredSection = _xform.StructuredSection
StructuredDocument = _xform.StructuredDocument
TradingFeatures = _xform.TradingFeatures
transform_document = _xform.transform_document
split_for_sentiment = _xform.split_for_sentiment
split_for_summarization = _xform.split_for_summarization
compute_trading_features = _xform.compute_trading_features
