import sys
import os
import pandas as pd
import numpy as np

# Ensure the parent directory (where features.py lives) is on sys.path for imports
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))
from features import compute_features


def test_compute_features_flat_series():
    # Flat price series -> all returns/log_returns/vol/momentum should be zero, sma equal to price
    prices = [100.0] * 10
    df = pd.DataFrame({"close": prices})
    feats = compute_features(df, price_col="close", window_short=3, window_long=5)

    # All prices preserved
    assert (feats["price"] == 100.0).all()

    # returns and log_return are zero
    assert np.allclose(feats["return"].values, 0.0)
    assert np.allclose(feats["log_return"].values, 0.0)

    # sma_short and sma_long equal to price after fillna -> 100
    assert np.allclose(feats["sma_short"].values, 100.0)
    assert np.allclose(feats["sma_long"].values, 100.0)

    # sma_ratio = 1.0
    assert np.allclose(feats["sma_ratio"].values, 1.0)

    # vol_short and vol_long zero
    assert np.allclose(feats["vol_short"].values, 0.0)
    assert np.allclose(feats["vol_long"].values, 0.0)

    # momentum zero
    assert np.allclose(feats["momentum"].values, 0.0)


def test_compute_features_increasing_momentum_and_returns():
    # Increasing price series to check momentum and returns are positive
    prices = list(range(1, 13))  # 1..12
    df = pd.DataFrame({"close": prices})
    window_short = 3
    window_long = 5
    feats = compute_features(df, price_col="close", window_short=window_short, window_long=window_long)

    # returns: first element 0 (fillna), others positive
    assert feats["return"].iloc[0] == 0.0
    assert (feats["return"].iloc[1:] > 0.0).all()

    # momentum: p - p.shift(window_short). For index >= window_short, equals window_short
    # e.g., price 4 at idx=3 -> 4 - 1 = 3
    for idx in range(window_short, len(prices)):
        expected = prices[idx] - prices[idx - window_short]
        assert feats["momentum"].iloc[idx] == expected

    # sma_ratio is finite and non-negative
    assert (feats["sma_ratio"].values >= 0.0).all()
