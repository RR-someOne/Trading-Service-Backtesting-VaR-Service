import sys
import os
import pandas as pd
import numpy as np

# Compute path to features.py under src/main/java/com/trading/service/strategy/python-ml
repo_src = os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..', '..'))
features_dir = os.path.join(repo_src, 'main', 'java', 'com', 'trading', 'service', 'strategy', 'python-ml')
sys.path.insert(0, features_dir)
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


def test_compute_features_missing_price_column():
    df = pd.DataFrame({"open": [1, 2, 3]})
    try:
        compute_features(df, price_col="close")
        assert False, "Expected ValueError for missing price_col"
    except ValueError:
        pass


def test_compute_features_non_numeric_coercion():
    # non-numeric prices get coerced to NaN then filled; ensure function runs and returns numeric features
    df = pd.DataFrame({"close": ["100", "101", "bad", "103"]})
    feats = compute_features(df, price_col="close", window_short=2, window_long=3)
    # All feature columns present and numeric
    for col in ["price", "return", "log_return", "sma_short", "sma_long", "sma_ratio", "vol_short", "vol_long", "momentum"]:
        assert col in feats.columns
        assert np.isfinite(feats[col].iloc[-1]) or feats[col].iloc[-1] == 0.0


def test_compute_features_invalid_windows():
    df = pd.DataFrame({"close": [100, 101, 102]})
    # window_short=0 is coerced to 1 in the implementation, so it should not raise
    feats = compute_features(df, window_short=0, window_long=5)
    assert "price" in feats.columns

    # non-integer window_long should raise ValueError
    try:
        compute_features(df, window_short=3, window_long="bad")
        assert False, "Expected ValueError for non-integer window_long"
    except ValueError:
        pass


def test_compute_features_negative_and_zero_prices():
    # negative or zero prices should not crash log calculation; features should be numeric
    df = pd.DataFrame({"close": [100.0, 0.0, -5.0, 50.0]})
    feats = compute_features(df, price_col="close", window_short=2, window_long=3)
    for col in ["price", "return", "log_return", "sma_short", "sma_long", "sma_ratio", "vol_short", "vol_long", "momentum"]:
        assert col in feats.columns
        # ensure numeric (NaNs converted to 0.0 per implementation)
        assert not feats[col].isnull().any()
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
