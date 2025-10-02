import json
import os
import numpy as np
import pandas as pd
from sklearn.ensemble import RandomForestClassifier
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import StandardScaler
from sklearn.model_selection import train_test_split
from skl2onnx import convert_sklearn
from skl2onnx.common.data_types import FloatTensorType
import onnx

from features import compute_features

# Settings
INPUT_CSV = "data/historical_prices.csv"  # must have columns: date, close
MODEL_DIR = "models"
MODEL_NAME = "rf_signal"
MODEL_VERSION = "v1"
ONNX_PATH = os.path.join(MODEL_DIR, MODEL_NAME + "_" + MODEL_VERSION + ".onnx")
METADATA_PATH = os.path.join(MODEL_DIR, MODEL_NAME + "_" + MODEL_VERSION + ".json")

os.makedirs(MODEL_DIR, exist_ok=True)

def build_labels(df_prices: pd.DataFrame, horizon: int = 1, thr: float = 0.001):
    # label = 1 (BUY) if next-horizon return > thr
    # label = -1 (SELL) if next-horizon return < -thr
    # label = 0 (HOLD) otherwise
    p = df_prices["close"]
    future_ret = p.shift(-horizon) / p - 1.0
    labels = pd.Series(0, index=df_prices.index)
    labels[future_ret > thr] = 1
    labels[future_ret < -thr] = -1
    # drop last horizon rows (NaNs)
    labels = labels.fillna(0).astype(int)
    return labels

def main():
    df = pd.read_csv(INPUT_CSV, parse_dates=["date"])
    df = df.sort_values("date").reset_index(drop=True)
    features = compute_features(df, price_col="close")
    labels = build_labels(df, horizon=1, thr=0.001)

    X = features[["return","log_return","sma_ratio","vol_short","vol_long","momentum"]].values
    y = labels.values

    # trim last rows where label might be produced from future NaN
    max_idx = len(y) - 1
    X = X[:max_idx]
    y = y[:max_idx]

    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, shuffle=False)

    pipe = Pipeline([
        ("scaler", StandardScaler()),
        ("clf", RandomForestClassifier(n_estimators=200, random_state=42, n_jobs=-1))
    ])
    pipe.fit(X_train, y_train)
    print("Train score:", pipe.score(X_train, y_train))
    print("Test score:", pipe.score(X_test, y_test))

    # Convert pipeline to ONNX
    initial_type = [("float_input", FloatTensorType([None, X.shape[1]]))]
    onnx_model = convert_sklearn(pipe, initial_types=initial_type)
    onnx.save_model(onnx_model, ONNX_PATH)
    print("Saved ONNX model to", ONNX_PATH)

    # Save metadata
    metadata = {
        "name": MODEL_NAME,
        "version": MODEL_VERSION,
        "framework": "scikit-learn",
        "created_by": "train_sklearn_to_onnx.py",
        "input_features": ["return","log_return","sma_ratio","vol_short","vol_long","momentum"],
        "label_mapping": {"-1": "SELL", "0": "HOLD", "1": "BUY"},
    }
    with open(METADATA_PATH, "w") as f:
        json.dump(metadata, f, indent=2)
    print("Saved metadata to", METADATA_PATH)

if __name__ == "__main__":
    main()
