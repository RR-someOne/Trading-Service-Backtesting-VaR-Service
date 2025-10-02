import os
import json
import numpy as np
import pandas as pd
import torch
import torch.nn as nn
import torch.optim as optim

from features import compute_features

INPUT_CSV = "data/historical_prices.csv"
MODEL_DIR = "models"
MODEL_NAME = "mlp_signal"
MODEL_VERSION = "v1"
ONNX_PATH = os.path.join(MODEL_DIR, MODEL_NAME + "_" + MODEL_VERSION + ".onnx")
METADATA_PATH = os.path.join(MODEL_DIR, MODEL_NAME + "_" + MODEL_VERSION + ".json")
os.makedirs(MODEL_DIR, exist_ok=True)

class MLP(nn.Module):
    def __init__(self, input_dim, hidden=64, output_dim=3):
        super().__init__()
        self.net = nn.Sequential(
            nn.Linear(input_dim, hidden),
            nn.ReLU(),
            nn.Linear(hidden, hidden),
            nn.ReLU(),
            nn.Linear(hidden, output_dim)
        )

    def forward(self, x):
        return self.net(x)

def build_labels(df_prices, horizon=1, thr=0.001):
    p = df_prices["close"]
    future_ret = p.shift(-horizon) / p - 1.0
    y = np.zeros(len(p), dtype=np.int64)
    y[future_ret > thr] = 2  # BUY -> class 2
    y[future_ret < -thr] = 0 # SELL -> class 0
    # HOLD -> class 1
    return y

def main():
    df = pd.read_csv(INPUT_CSV, parse_dates=["date"]).sort_values("date").reset_index(drop=True)
    features = compute_features(df)
    feature_cols = ["return","log_return","sma_ratio","vol_short","vol_long","momentum"]
    X = features[feature_cols].values[:-1]
    y = build_labels(df)[:-1]

    # to torch
    X_t = torch.tensor(X, dtype=torch.float32)
    y_t = torch.tensor(y, dtype=torch.long)

    model = MLP(input_dim=X.shape[1], hidden=64, output_dim=3)
    loss_fn = nn.CrossEntropyLoss()
    opt = optim.Adam(model.parameters(), lr=1e-3)

    for epoch in range(50):
        model.train()
        logits = model(X_t)
        loss = loss_fn(logits, y_t)
        opt.zero_grad()
        loss.backward()
        opt.step()
        if epoch % 10 == 0:
            pred = logits.argmax(dim=1)
            acc = (pred == y_t).float().mean().item()
            print(f"Epoch {epoch} loss={loss.item():.4f} acc={acc:.4f}")

    # Export to ONNX
    dummy = torch.randn(1, X.shape[1], dtype=torch.float32)
    torch.onnx.export(model, dummy, ONNX_PATH,
                      input_names=["float_input"],
                      output_names=["logits"],
                      opset_version=13)
    print("Saved ONNX to", ONNX_PATH)

    metadata = {
        "name": MODEL_NAME,
        "version": MODEL_VERSION,
        "framework": "pytorch",
        "input_features": feature_cols
    }
    with open(METADATA_PATH, "w") as f:
        json.dump(metadata, f, indent=2)
    print("Saved metadata to", METADATA_PATH)

if __name__ == "__main__":
    main()
