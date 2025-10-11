from dataclasses import dataclass

try:
    from optimum.onnxruntime import ORTModelForSeq2SeqLM  # type: ignore
    from transformers import AutoTokenizer  # type: ignore
except Exception:  # pragma: no cover
    ORTModelForSeq2SeqLM = None  # type: ignore
    AutoTokenizer = None  # type: ignore


@dataclass
class SummaryOutput:
    summary_text: str


class OnnxBartSummarizer:
    """ONNX Runtime-based summarizer using Optimum for BART.

    Uses ORTModelForSeq2SeqLM with export=True so it can export on first use if needed.
    Requires `optimum[onnxruntime]` and `transformers`.
    """

    def __init__(self, model_name: str = "facebook/bart-large-cnn") -> None:
        if ORTModelForSeq2SeqLM is None or AutoTokenizer is None:
            raise RuntimeError("optimum[onnxruntime] and transformers not available")
        self.tokenizer = AutoTokenizer.from_pretrained(model_name)
        # export=True converts the model to ONNX if no ONNX weights are found
        self.model = ORTModelForSeq2SeqLM.from_pretrained(model_name, export=True)

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
