import pandas as pd
import numpy as np

def compute_features(df, price_col="close", window_short=5, window_long=20):
    """
    Returns DataFrame with deterministic features used by both classifier & VaR model. 
    """
    # Validate inputs
    if price_col not in df.columns:
        raise ValueError(f"price_col '{price_col}' not found in DataFrame columns")

    # Ensure windows are sensible integers
    try:
        window_short = max(1, int(window_short))
        window_long = max(window_short, int(window_long))
    except Exception:
        raise ValueError("window_short/window_long must be integers >= 1")

    # Coerce prices to numeric, handle non-numeric by converting to NaN
    p = pd.to_numeric(df[price_col], errors='coerce')
    # Fill short gaps by forward-fill then back-fill; finally replace remaining NaN with 0.0
    p = p.ffill().bfill().fillna(0.0).astype(float)

    # Percentage and log returns, fill initial NaNs with 0.0
    ret = p.pct_change().fillna(0.0)
    # For log, protect against non-positive values by using np.log on positive values only;
    # negative or zero prices (after coercion) produce -inf/NaN which we then fill with 0.0
    with np.errstate(divide='ignore', invalid='ignore'):
        logret = np.log(p).diff()
    logret = logret.replace([np.inf, -np.inf], np.nan).fillna(0.0)

    # Rolling statistics using min_periods=1 so we get immediate values even for small windows
    sma_short = p.rolling(window_short, min_periods=1).mean()
    sma_long = p.rolling(window_long, min_periods=1).mean()
    sma_ratio = (sma_short / sma_long).replace([np.inf, -np.inf], np.nan).fillna(0.0)
    vol_short = ret.rolling(window_short, min_periods=1).std(ddof=0).fillna(0.0)
    vol_long = ret.rolling(window_long, min_periods=1).std(ddof=0).fillna(0.0)
    momentum = p - p.shift(window_short)

    features = pd.DataFrame({
        "price": p,
        "return": ret,
        "log_return": logret,
        "sma_short": sma_short,
        "sma_long": sma_long,
        "sma_ratio": sma_ratio,
        "vol_short": vol_short,
        "vol_long": vol_long,
        "momentum": momentum.fillna(0.0)
    }, index=df.index).fillna(0.0)

    return features