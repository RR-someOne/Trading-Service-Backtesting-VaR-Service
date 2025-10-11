from dataclasses import dataclass
from typing import Optional

try:
    from transformers import AutoTokenizer, AutoModelForSequenceClassification
    import torch
except Exception:  # pragma: no cover
    AutoTokenizer = None
    AutoModelForSequenceClassification = None
    torch = None


@dataclass
class SentimentOutput:
    label: str
    score: float


class GPTClassifier:
    """Generic GPT-style classifier head fine-tuned for sentiment.

    Note: This is a placeholder that uses a sequence classifier compatible with GPT architectures.
    Provide a proper fine-tuned model id via `model_name`.
    """

    def __init__(self, model_name: str) -> None:
        if AutoTokenizer is None:
            raise RuntimeError("transformers/torch not available in this environment")
        self.tokenizer = AutoTokenizer.from_pretrained(model_name)
        self.model = AutoModelForSequenceClassification.from_pretrained(model_name)
        self.model.eval()

    @torch.inference_mode()
    def predict(self, text: str) -> SentimentOutput:
        if not text:
            return SentimentOutput(label="NEUTRAL", score=0.5)
        inputs = self.tokenizer(text, return_tensors="pt", truncation=True)
        outputs = self.model(**inputs)
        probs = torch.softmax(outputs.logits, dim=-1)[0]
        idx = int(torch.argmax(probs).item())
        labels = ["NEGATIVE", "NEUTRAL", "POSITIVE"]
        return SentimentOutput(label=labels[idx], score=float(probs[idx].item()))
