from dataclasses import dataclass
from typing import Literal, Optional

# Runtime-optional deps
try:
    from .finbert_model import FinBertSentiment as TorchFinBert
except Exception:  # pragma: no cover
    TorchFinBert = None

try:
    from .finbert_tf_model import FinBertTFSentiment as TFFinBert
except Exception:  # pragma: no cover
    TFFinBert = None


@dataclass
class SentimentOutput:
    label: str
    score: float


class SentimentPipeline:
    """Unified interface to choose a backend: 'torch' or 'tf' FinBERT.

    Example:
        pipe = SentimentPipeline(backend="torch")
        print(pipe("Great growth, record profits!"))
    """

    def __init__(self, backend: Literal["torch", "tf"] = "torch") -> None:
        self.backend = backend
        if backend == "torch":
            if TorchFinBert is None:
                raise RuntimeError("PyTorch FinBERT not available. Install transformers+torch.")
            self.impl = TorchFinBert()
        elif backend == "tf":
            if TFFinBert is None:
                raise RuntimeError("TensorFlow FinBERT not available. Install transformers+tensorflow.")
            self.impl = TFFinBert()
        else:
            raise ValueError(f"Unknown backend: {backend}")

    def __call__(self, text: str) -> SentimentOutput:
        out = self.impl.predict(text)
        return SentimentOutput(label=out.label, score=out.score)
