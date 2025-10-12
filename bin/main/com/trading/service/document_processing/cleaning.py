import re
from html import unescape
from typing import Iterable

_WS_RE = re.compile(r"\s+")
_HTML_TAG_RE = re.compile(r"<[^>]+>")


def strip_html(text: str) -> str:
    if not text:
        return ""
    text = unescape(text)
    text = _HTML_TAG_RE.sub(" ", text)
    return _WS_RE.sub(" ", text).strip()


def normalize_whitespace(text: str) -> str:
    return _WS_RE.sub(" ", text or "").strip()


def remove_boilerplate(text: str, patterns: Iterable[str] | None = None) -> str:
    if not text:
        return ""
    patterns = patterns or [
        r"forward-looking statements",
        r"safe harbor",
        r"all rights reserved",
        r"copyright \d{4}",
    ]
    out = text
    for p in patterns:
        out = re.sub(p, " ", out, flags=re.IGNORECASE)
    return normalize_whitespace(out)
