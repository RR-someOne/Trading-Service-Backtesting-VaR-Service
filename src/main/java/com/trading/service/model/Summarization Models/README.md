# Summarization Models (Python)

Wrappers around HuggingFace Transformers for summarizing financial documents.

Models:
- facebook/bart-large-cnn (BartSummarizer)
- t5-base (T5Summarizer)

Install:

```bash
pip install transformers torch
```

Usage:

```python
from com.trading.service.model.Summarization Models.bart_model import BartSummarizer

s = BartSummarizer()
print(s.summarize("Q3 10-Q: Revenue grew 12% YoY..."))
```
