def sample_10k_text():
    return (
        "Acme Corp 2025 Annual Report\n"
        "Item 1. Business\n\nOur business focuses on widgets. Revenue increased.\n\n"
        "Item 1A. Risk Factors\n\nWe face risk from supply chain disruptions and inflation.\n\n"
        "Item 7. Management's Discussion and Analysis\n\nRevenue grew and guidance improved due to strong demand."
    )


def test_transform_and_splits():
    from model.document_processing import (
        transform_document,
        split_for_sentiment,
        split_for_summarization,
        compute_trading_features,
    )

    text = sample_10k_text()
    doc = transform_document(text, doc_type="10-K", metadata={"ticker": "ACME"})

    # Basic structure
    assert doc.title.startswith("Acme Corp")
    assert "risk" in doc.sections
    assert "md&a" in doc.sections

    # Sentiment chunks
    chunks = split_for_sentiment(doc, granularity="paragraph")
    assert any("guidance" in c.lower() for c in chunks)

    # Summarization focus
    sum_text = split_for_summarization(doc)
    assert "management" in sum_text.lower() or "risk" in sum_text.lower()

    # Features
    feats = compute_trading_features(doc)
    assert feats.overall.tokens > 0
    assert "risk" in feats.by_section