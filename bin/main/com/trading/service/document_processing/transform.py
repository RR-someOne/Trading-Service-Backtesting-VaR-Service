from __future__ import annotations

from dataclasses import dataclass
from typing import Dict, Iterable, List, Literal, Optional, Tuple
import re

from .cleaning import (
    normalize_whitespace_preserve_lines,
    remove_boilerplate_preserve_lines,
    strip_html_preserve_lines,
)
from .parsing import Section, detect_sections, extract_title_and_abstract
from .features import DocFeatures, compute_features


# Canonical section keys we care about
CANON_KEYS = {
    "md&a": [
        r"management's\s+discussion\s+and\s+analysis",
        r"management\s+discussion",
        r"item\s+7[\.]?",
    ],
    "risk": [
        r"risk\s+factors",
        r"item\s+1a[\.]?",
    ],
    "business": [
        r"business",
        r"item\s+1[\.]?",
    ],
    "results": [
        r"results\s+of\s+operations",
    ],
}

_KEY_PATTERNS: Dict[str, List[re.Pattern[str]]] = {
    k: [re.compile(p, re.IGNORECASE) for p in pats] for k, pats in CANON_KEYS.items()
}


@dataclass
class StructuredSection:
    key: str
    title: str
    paragraphs: List[str]

    @property
    def text(self) -> str:
        return "\n\n".join(self.paragraphs)


@dataclass
class StructuredDocument:
    doc_type: str
    title: str
    abstract: str
    cleaned_text: str
    sections: Dict[str, StructuredSection]
    metadata: Dict[str, str]


@dataclass
class TradingFeatures:
    overall: DocFeatures
    by_section: Dict[str, DocFeatures]


def _canonical_key(title: str) -> Optional[str]:
    t = (title or "").lower()
    for key, patterns in _KEY_PATTERNS.items():
        if any(p.search(t) for p in patterns):
            return key
    return None


def transform_document(
    text: str,
    doc_type: Literal["10-K", "10-Q", "press_release", "investor_letter", "earnings_call"] = "10-K",
    metadata: Optional[Dict[str, str]] = None,
) -> StructuredDocument:
    # 1) Clean
    html_stripped = strip_html_preserve_lines(text)
    baseline = normalize_whitespace_preserve_lines(remove_boilerplate_preserve_lines(html_stripped))

    # 2) Title/abstract
    title, abstract = extract_title_and_abstract(baseline)

    # 3) Sections detection
    secs = detect_sections(baseline)
    structured: Dict[str, StructuredSection] = {}
    for s in secs:
        key = _canonical_key(s.title) or "general"
        # Merge paragraphs under same canonical key
        if key in structured:
            structured[key].paragraphs.extend(s.paragraphs)
            # Keep the first encountered title
        else:
            structured[key] = StructuredSection(key=key, title=s.title, paragraphs=list(s.paragraphs))

    if "general" not in structured and secs:
        # Fallback to a general section if nothing matched
        structured["general"] = StructuredSection(key="general", title="General", paragraphs=[sec for s in secs for sec in s.paragraphs])

    return StructuredDocument(
        doc_type=doc_type,
        title=title,
        abstract=abstract,
        cleaned_text=baseline,
        sections=structured,
        metadata=metadata or {},
    )


def split_for_sentiment(
    doc: StructuredDocument,
    granularity: Literal["paragraph", "section"] = "paragraph",
    focus: Optional[Iterable[str]] = None,
    max_items: int = 200,
) -> List[str]:
    keys = set(k.lower() for k in (focus or doc.sections.keys()))
    chunks: List[str] = []
    for k, s in doc.sections.items():
        if keys and k.lower() not in keys:
            continue
        if granularity == "section":
            chunks.append(s.text)
        else:
            chunks.extend(p for p in s.paragraphs if p)
        if len(chunks) >= max_items:
            return chunks[:max_items]
    return chunks[:max_items]


def split_for_summarization(
    doc: StructuredDocument,
    focus: Iterable[str] = ("md&a", "risk"),
    max_chars: int = 4000,
) -> str:
    keys = [k.lower() for k in focus]
    texts: List[str] = []
    for k in keys:
        if k in doc.sections:
            texts.append(doc.sections[k].text)
    if not texts:  # fallback to whole cleaned text
        return doc.cleaned_text[:max_chars]
    out = "\n\n".join(texts)
    return out[:max_chars]


def compute_trading_features(
    doc: StructuredDocument,
    keywords: Optional[Iterable[str]] = None,
) -> TradingFeatures:
    overall = compute_features(doc.cleaned_text, keywords=keywords)
    by_sec: Dict[str, DocFeatures] = {}
    for k, s in doc.sections.items():
        by_sec[k] = compute_features(s.text, keywords=keywords)
    return TradingFeatures(overall=overall, by_section=by_sec)
