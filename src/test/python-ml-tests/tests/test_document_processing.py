import pytest


def test_strip_html_and_whitespace():
    from model.document_processing import strip_html, normalize_whitespace

    html = "<h1>Title</h1>   <p> Hello   world </p>"
    out = strip_html(html)
    assert out == "Title Hello world"

    assert normalize_whitespace("  a\n\n b \t c  ") == "a b c"


def test_remove_boilerplate():
    from model.document_processing import remove_boilerplate

    text = "This contains forward-looking statements and All Rights Reserved 2025."
    out = remove_boilerplate(text)
    assert "forward-looking" not in out.lower()
    assert "rights reserved" not in out.lower()


def test_paragraph_split_and_sections():
    from model.document_processing import split_paragraphs, detect_sections

    text = "Item 1. Risk Factors\n\nWe face various risks.\n\nManagement Discussion\n\nWe performed well."
    paras = split_paragraphs(text)
    assert len(paras) == 4

    secs = detect_sections(text)
    assert len(secs) >= 2
    assert any("Risk Factors" in s.title for s in secs)


def test_extract_title_and_abstract():
    from model.document_processing import extract_title_and_abstract

    text = "Main Title\nFirst line abstract.\nSecond line."
    title, abstract = extract_title_and_abstract(text)
    assert title == "Main Title"
    assert abstract.startswith("First line abstract.")


def test_compute_features_basic():
    from model.document_processing import compute_features

    text = "Revenue grew. Guidance increased. Risk decreased."
    feats = compute_features(text)
    assert feats.tokens > 0
    assert feats.sentences >= 1
    assert feats.avg_tokens_per_sentence > 0
    # Keywords present
    assert feats.keyword_freq["revenue"] >= 1
