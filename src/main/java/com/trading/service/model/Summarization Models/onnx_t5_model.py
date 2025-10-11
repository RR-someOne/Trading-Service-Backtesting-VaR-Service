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


class OnnxT5Summarizer:
    """ONNX Runtime-based summarizer using Optimum for T5.

    Requires `optimum[onnxruntime]` and `transformers`.
    """

    def __init__(self, model_name: str = "t5-base") -> None:
        if ORTModelForSeq2SeqLM is None or AutoTokenizer is None:
            raise RuntimeError("optimum[onnxruntime] and transformers not available")
        self.tokenizer = AutoTokenizer.from_pretrained(model_name)
        self.model = ORTModelForSeq2SeqLM.from_pretrained(model_name, export=True)

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
