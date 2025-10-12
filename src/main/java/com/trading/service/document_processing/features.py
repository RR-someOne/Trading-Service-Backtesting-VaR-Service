from __future__ import annotations

from dataclasses import dataclass
from typing import Dict, Iterable
import re

_SENT_RE = re.compile(r"[.!?]+\s+")
_WORD_RE = re.compile(r"\b\w+\b")


@dataclass
class DocFeatures:
    tokens: int
    sentences: int
    avg_tokens_per_sentence: float
    keyword_freq: Dict[str, int]


def compute_features(text: str, keywords: Iterable[str] | None = None) -> DocFeatures:
    text = text or ""
    sentences = len([s for s in _SENT_RE.split(text) if s.strip()]) or 1
    tokens = len(_WORD_RE.findall(text))
    avg = tokens / sentences if sentences else 0.0
    keywords = [k.lower() for k in (keywords or ["revenue", "guidance", "inflation", "risk", "growth"])]
    token_l = [t.lower() for t in _WORD_RE.findall(text)]
    freq: Dict[str, int] = {k: 0 for k in keywords}
    for t in token_l:
        if t in freq:
            freq[t] += 1
    return DocFeatures(tokens=tokens, sentences=sentences, avg_tokens_per_sentence=avg, keyword_freq=freq)
