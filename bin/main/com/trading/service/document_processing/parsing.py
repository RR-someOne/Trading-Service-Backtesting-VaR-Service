from dataclasses import dataclass
from typing import List, Tuple
import re


_HEADER_RE = re.compile(r"^(item\s+\d+[a-z]?\.?|management\s+discussion|risk\s+factors)\b", re.IGNORECASE)


@dataclass
class Section:
    title: str
    paragraphs: List[str]


def split_paragraphs(text: str) -> List[str]:
    parts = [p.strip() for p in re.split(r"\n\s*\n+", text or "")]
    return [p for p in parts if p]


def detect_sections(text: str) -> List[Section]:
    paras = split_paragraphs(text)
    sections: List[Section] = []
    current_title = "General"
    current_paras: List[str] = []
    for p in paras:
        if _HEADER_RE.match(p.split("\n", 1)[0]):
            if current_paras:
                sections.append(Section(title=current_title, paragraphs=current_paras))
            current_title = p.split("\n", 1)[0][:120]
            current_paras = []
        else:
            current_paras.append(p)
    if current_paras:
        sections.append(Section(title=current_title, paragraphs=current_paras))
    return sections


def extract_title_and_abstract(text: str) -> Tuple[str, str]:
    lines = [ln.strip() for ln in (text or "").splitlines() if ln.strip()]
    title = lines[0] if lines else ""
    abstract = " ".join(lines[1:5])[:1000]
    return title, abstract
