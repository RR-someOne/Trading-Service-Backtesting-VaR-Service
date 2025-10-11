from dataclasses import dataclass
from typing import Optional

try:
    from transformers import BartForConditionalGeneration, BartTokenizer
    import torch
except Exception:  # pragma: no cover
    BartForConditionalGeneration = None
    BartTokenizer = None
    torch = None


@dataclass
class SummaryOutput:
    summary_text: str


class BartSummarizer:
    """Summarization wrapper using facebook/bart-large-cnn."""

    def __init__(self, model_name: str = "facebook/bart-large-cnn") -> None:
        if BartTokenizer is None or BartForConditionalGeneration is None:
            raise RuntimeError("transformers/torch not available")
        self.tokenizer = BartTokenizer.from_pretrained(model_name)
        self.model = BartForConditionalGeneration.from_pretrained(model_name)
        self.model.eval()

    @torch.inference_mode()
    def summarize(self, text: str, max_length: int = 200, min_length: int = 30) -> SummaryOutput:
        if not text:
            return SummaryOutput(summary_text="")
        inputs = self.tokenizer([text], max_length=1024, truncation=True, return_tensors="pt")
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
