# Sentiment Analysis Models (Python)

This module provides thin wrappers around HuggingFace Transformers to score financial text sentiment.

Models included:
- FinBERT (ProsusAI/finbert)
- BERT (e.g., yiyanghkust/finbert-tone)
- GPT variants (provide your own fine-tuned model id)

Usage example:

```python
from model.Sentiment Analysis Models.finbert_model import FinBertSentiment

clf = FinBertSentiment()
print(clf.predict("The company posted record profits and strong growth"))
```

Note: Requires `pip install transformers torch` and an internet connection to download models.
