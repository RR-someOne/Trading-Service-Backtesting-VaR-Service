from dataclasses import dataclass
from typing import Optional

try:
    from transformers import T5ForConditionalGeneration, T5Tokenizer
    import torch
except Exception:  # pragma: no cover
    T5ForConditionalGeneration = None
    T5Tokenizer = None
    torch = None


@dataclass
class SummaryOutput:
    summary_text: str


class T5Summarizer:
    """Summarization wrapper using t5-base."""

    def __init__(self, model_name: str = "t5-base") -> None:
        if T5Tokenizer is None or T5ForConditionalGeneration is None:
            raise RuntimeError("transformers/torch not available")
        self.tokenizer = T5Tokenizer.from_pretrained(model_name)
        self.model = T5ForConditionalGeneration.from_pretrained(model_name)
        self.model.eval()

    @torch.inference_mode()
    def summarize(self, text: str, max_length: int = 200, min_length: int = 30) -> SummaryOutput:
        if not text:
            return SummaryOutput(summary_text="")
        prefixed = "summarize: " + text
        inputs = self.tokenizer([prefixed], max_length=1024, truncation=True, return_tensors="pt")
        summary_ids = self.model.generate(
            inputs["input_ids"],
            num_beams=4,
            length_penalty=2.0,
            max_length=max_length,
            min_length=min_length,
            early_stopping=True,
        )
        summary = self.tokenizer.batch_decode(summary_ids, skip_special_tokens=True)[0]
        return SummaryOutput(summary_text=summary)
