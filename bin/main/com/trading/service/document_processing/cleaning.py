import re
from html import unescape
from typing import Iterable

_WS_RE = re.compile(r"\s+")
_INLINE_WS_RE = re.compile(r"[ \t]+")
_HTML_TAG_RE = re.compile(r"<[^>]+>")


def strip_html(text: str) -> str:
    if not text:
        return ""
    text = unescape(text)
    text = _HTML_TAG_RE.sub(" ", text)
    return _WS_RE.sub(" ", text).strip()


def normalize_whitespace(text: str) -> str:
    return _WS_RE.sub(" ", text or "").strip()


def normalize_whitespace_preserve_lines(text: str) -> str:
    """Normalize spaces and tabs but preserve line breaks.

    - Collapses consecutive spaces/tabs to a single space
    - Trims spaces around line breaks
    - Collapses 3+ blank lines to max 2
    """
    if not text:
        return ""
    # Normalize inline whitespace
    lines = [
        _INLINE_WS_RE.sub(" ", ln).strip()
        for ln in (text.replace("\r\n", "\n").replace("\r", "\n").split("\n"))
    ]
    # Collapse excessive blank lines
    out_lines = []
    blank_run = 0
    for ln in lines:
        if ln == "":
            blank_run += 1
            if blank_run <= 2:
                out_lines.append("")
        else:
            blank_run = 0
            out_lines.append(ln)
    return "\n".join(out_lines).strip()


def strip_html_preserve_lines(text: str) -> str:
    if not text:
        return ""
    text = unescape(text)
    text = _HTML_TAG_RE.sub(" ", text)
    return normalize_whitespace_preserve_lines(text)


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


def remove_boilerplate_preserve_lines(text: str, patterns: Iterable[str] | None = None) -> str:
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
    return normalize_whitespace_preserve_lines(out)
