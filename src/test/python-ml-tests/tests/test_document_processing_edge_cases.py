def test_empty_inputs():
    from model.document_processing import (
        strip_html,
        normalize_whitespace,
        remove_boilerplate,
        split_paragraphs,
        detect_sections,
        extract_title_and_abstract,
        compute_features,
    )

    assert strip_html("") == ""
    assert normalize_whitespace("") == ""
    assert remove_boilerplate("") == ""
    assert split_paragraphs("") == []
    assert detect_sections("") == []  # no content => no sections
    title, abstract = extract_title_and_abstract("")
    assert title == "" and abstract == ""
    feats = compute_features("")
    assert feats.tokens == 0 and feats.sentences == 1


def test_remove_boilerplate_custom_patterns():
    from model.document_processing import remove_boilerplate

    txt = "Confidential: INTERNAL USE ONLY. Forward-Looking Statements apply."
    cleaned = remove_boilerplate(txt, patterns=[r"internal use only", r"forward-looking statements"])
    assert "internal use only" not in cleaned.lower()
    assert "forward-looking" not in cleaned.lower()


def test_detect_sections_without_headers():
    from model.document_processing import detect_sections

    txt = "Para A.\n\nPara B.\n\nPara C."
    secs = detect_sections(txt)
    # Everything ends up in a single default section
    assert len(secs) == 1
    assert secs[0].title == "General"
    assert len(secs[0].paragraphs) == 3


def test_extract_title_and_abstract_limits():
    from model.document_processing import extract_title_and_abstract

    lines = ["T"] + [f"L{i}" for i in range(100)]
    txt = "\n".join(lines)
    title, abstract = extract_title_and_abstract(txt)
    assert title == "T"
    assert len(abstract) <= 1000


def test_compute_features_custom_keywords():
    from model.document_processing import compute_features

    txt = "Alpha beta Beta gamma ALPHA."
    feats = compute_features(txt, keywords=["alpha", "beta", "delta"])
    assert feats.keyword_freq["alpha"] == 2
    assert feats.keyword_freq["beta"] == 2
    assert feats.keyword_freq["delta"] == 0
