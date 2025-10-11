import types
import pytest


def test_import_pipeline_alias():
    # Should import without requiring transformers/torch
    from model.summarization_models import SummarizationPipeline  # noqa: F401


def test_unknown_backend_raises():
    from model.summarization_models import SummarizationPipeline

    with pytest.raises(ValueError):
        SummarizationPipeline(backend="unknown")


class _DummyOut:
    def __init__(self, text: str) -> None:
        self.summary_text = text


class _FakeSummarizer:
    def __init__(self, *args, **kwargs) -> None:  # signature compatible
        pass

    def summarize(self, text: str, max_length: int = 200, min_length: int = 30):
        # Return a dummy object matching the expected interface
        return _DummyOut("DUMMY")


def test_bart_backend_with_monkeypatch(monkeypatch):
    # Patch the underlying space module to use a fake summarizer avoiding heavy deps
    import model.summarization_models as pkg
    import model.summarization_models.pipeline as sp

    monkeypatch.setattr(pkg._space, "BartSummarizer", _FakeSummarizer, raising=False)

    pipe = sp.SummarizationPipeline(backend="bart")
    out = pipe("Some long financial text for summarization.")
    assert out.summary_text == "DUMMY"


def test_t5_backend_with_monkeypatch(monkeypatch):
    import model.summarization_models as pkg
    import model.summarization_models.pipeline as sp

    monkeypatch.setattr(pkg._space, "T5Summarizer", _FakeSummarizer, raising=False)

    pipe = sp.SummarizationPipeline(backend="t5")
    out = pipe("Another long 10-K narrative.")
    assert out.summary_text == "DUMMY"
