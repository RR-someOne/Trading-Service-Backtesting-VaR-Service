from dataclasses import dataclass
from typing import Optional

try:
    import tensorflow as tf  # type: ignore
    from transformers import AutoTokenizer, TFAutoModelForSequenceClassification
except Exception:  # pragma: no cover
    tf = None
    AutoTokenizer = None
    TFAutoModelForSequenceClassification = None


@dataclass
class SentimentOutput:
    label: str
    score: float


class FinBertTFSentiment:
    """
    TensorFlow wrapper attempting to load ProsusAI/finbert.

    Note: Some checkpoints are PyTorch-only. We try from_pt=True when necessary.
    If this fails, consider using the PyTorch version in finbert_model.FinBertSentiment.
    """

    def __init__(self, model_name: str = "ProsusAI/finbert") -> None:
        if tf is None or AutoTokenizer is None or TFAutoModelForSequenceClassification is None:
            raise RuntimeError("tensorflow/transformers not available in this environment")
        self.tokenizer = AutoTokenizer.from_pretrained(model_name)
        try:
            self.model = TFAutoModelForSequenceClassification.from_pretrained(model_name)
        except Exception:
            # Fallback: load from PyTorch weights
            self.model = TFAutoModelForSequenceClassification.from_pretrained(model_name, from_pt=True)

    def predict(self, text: str) -> SentimentOutput:
        if not text:
            return SentimentOutput(label="NEUTRAL", score=0.5)
        inputs = self.tokenizer(text, return_tensors="tf", truncation=True)
        outputs = self.model(**inputs)
        probs = tf.nn.softmax(outputs.logits, axis=-1)[0]
        idx = int(tf.argmax(probs).numpy())
        labels = ["NEGATIVE", "NEUTRAL", "POSITIVE"]
        return SentimentOutput(label=labels[idx], score=float(probs[idx].numpy()))
