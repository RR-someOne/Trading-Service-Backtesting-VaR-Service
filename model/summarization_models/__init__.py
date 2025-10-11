# Import alias for Summarization Models with spaces
from pathlib import Path
import importlib.util
import sys

# Compute repo root and space-dir safely
_REPO_ROOT = Path(__file__).resolve().parents[2]
_SPACE_DIR = _REPO_ROOT / 'src' / 'main' / 'java' / 'com' / 'trading' / 'service' / 'model' / 'Summarization Models'

# Load the real pipeline as a proper submodule so relative imports inside it can fail gracefully
_spec = importlib.util.spec_from_file_location('model.summarization_models._space', str(_SPACE_DIR / 'pipeline.py'))
if _spec is None or _spec.loader is None:
	raise ImportError(f"Unable to locate pipeline at {_SPACE_DIR}")
_space = importlib.util.module_from_spec(_spec)
sys.modules[_spec.name] = _space
_spec.loader.exec_module(_space)  # type: ignore[arg-type]

# Re-export the public API
SummarizationPipeline = _space.SummarizationPipeline  # type: ignore[attr-defined]

# Intentionally do NOT eagerly import heavy model modules here to avoid torch/transformers dependency at import time.
# Tests can monkeypatch attributes on `model.summarization_models._space` as needed.
