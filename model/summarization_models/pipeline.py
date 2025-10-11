"""Expose the SummarizationPipeline object from the space-based package.

This allows `from model.summarization_models.pipeline import SummarizationPipeline` to work
without duplicating import logic. The heavy module is loaded by package __init__.
"""

from . import _space  # type: ignore  # loaded dynamically in __init__

SummarizationPipeline = _space.SummarizationPipeline  # type: ignore[attr-defined]
