from dataclasses import dataclass
from typing import Literal

try:
    from .bart_model import BartSummarizer
except Exception:  # pragma: no cover
    BartSummarizer = None

try:
    from .t5_model import T5Summarizer
except Exception:  # pragma: no cover
    T5Summarizer = None


@dataclass
class SummaryOutput:
    summary_text: str


class SummarizationPipeline:
    """Unified interface to switch between BART and T5 backends."""

    def __init__(self, backend: Literal["bart", "t5"] = "bart") -> None:
        self.backend = backend
        if backend == "bart":
            if BartSummarizer is None:
                raise RuntimeError("BART not available. Install transformers+torch.")
            self.impl = BartSummarizer()
        elif backend == "t5":
            if T5Summarizer is None:
                raise RuntimeError("T5 not available. Install transformers+torch.")
            self.impl = T5Summarizer()
        else:
            raise ValueError(f"Unknown backend: {backend}")

    def __call__(self, text: str, max_length: int = 200, min_length: int = 30) -> SummaryOutput:
        out = self.impl.summarize(text, max_length=max_length, min_length=min_length)
        return SummaryOutput(summary_text=out.summary_text)
