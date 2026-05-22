import json
from pathlib import Path

import numpy as np
import pandas as pd
import tensorflow as tf
from sklearn.model_selection import train_test_split

ROOT = Path(__file__).resolve().parents[1]
DATA_PATH = ROOT / "ml-training" / "dataset" / "ingredient_category.csv"
ASSET_DIR = ROOT / "app" / "src" / "main" / "assets" / "ml"
ASSET_DIR.mkdir(parents=True, exist_ok=True)

MODEL_PATH = ASSET_DIR / "ingredient_classifier.tflite"
LABEL_PATH = ASSET_DIR / "labels.txt"
VOCAB_PATH = ASSET_DIR / "vocabulary.json"

def clean(text: str) -> str:
    return "".join(
        ch for ch in str(text).strip().lower()
        if ch.isalnum() or "가" <= ch <= "힣"
    )

def tokens(text: str):
    text = clean(text)
    result = set()

    if not text:
        return result

    result.add(text)

    for ch in text:
        result.add(ch)

    for n in [2, 3]:
        if len(text) >= n:
            for i in range(len(text) - n + 1):
                result.add(text[i:i+n])

    return result

def augment_name(name: str):
    base = clean(name)
    variants = {
        base,
        f"국산{base}",
        f"신선{base}",
        f"냉장{base}",
        f"냉동{base}",
        f"{base}1개",
        f"{base}1봉",
        f"{base}500g",
        f"{base}1kg",
        f"{base}300g",
    }
    return list(variants)

def build_vector(text, vocab):
    vector = np.zeros(len(vocab), dtype=np.float32)
    for token in tokens(text):
        idx = vocab.get(token)
        if idx is not None:
            vector[idx] = 1.0
    return vector

def main():
    df = pd.read_csv(DATA_PATH)
    df["ingredient"] = df["ingredient"].astype(str)
    df["category"] = df["category"].astype(str)

    augmented_rows = []
    for _, row in df.iterrows():
        for name in augment_name(row["ingredient"]):
            augmented_rows.append({
                "ingredient": name,
                "category": row["category"]
            })

    df_aug = pd.DataFrame(augmented_rows).drop_duplicates()
    print(f"original rows: {len(df)}")
    print(f"augmented rows: {len(df_aug)}")

    labels = sorted(df_aug["category"].unique().tolist())
    label_to_id = {label: i for i, label in enumerate(labels)}

    vocab_tokens = sorted(set().union(*[tokens(x) for x in df_aug["ingredient"].tolist()]))
    vocab = {token: i for i, token in enumerate(vocab_tokens)}

    x = np.stack([build_vector(name, vocab) for name in df_aug["ingredient"].tolist()])
    y = np.array([label_to_id[label] for label in df_aug["category"].tolist()], dtype=np.int32)

    x_train, x_test, y_train, y_test = train_test_split(
        x,
        y,
        test_size=0.2,
        random_state=42,
        stratify=y
    )

    model = tf.keras.Sequential([
        tf.keras.layers.Input(shape=(x.shape[1],)),
        tf.keras.layers.Dense(128, activation="relu"),
        tf.keras.layers.Dropout(0.15),
        tf.keras.layers.Dense(64, activation="relu"),
        tf.keras.layers.Dense(len(labels), activation="softmax")
    ])

    model.compile(
        optimizer=tf.keras.optimizers.Adam(learning_rate=0.001),
        loss="sparse_categorical_crossentropy",
        metrics=["accuracy"]
    )

    model.fit(
        x_train,
        y_train,
        validation_data=(x_test, y_test),
        epochs=60,
        batch_size=16,
        verbose=1
    )

    loss, acc = model.evaluate(x_test, y_test, verbose=0)

    converter = tf.lite.TFLiteConverter.from_keras_model(model)
    tflite_model = converter.convert()

    MODEL_PATH.write_bytes(tflite_model)
    LABEL_PATH.write_text("\n".join(labels), encoding="utf-8")
    VOCAB_PATH.write_text(json.dumps(vocab, ensure_ascii=False, indent=2), encoding="utf-8")

    print(f"model saved: {MODEL_PATH}")
    print(f"labels saved: {LABEL_PATH}")
    print(f"vocabulary saved: {VOCAB_PATH}")
    print(f"test accuracy: {acc:.4f}")

    test_samples = [
        "계란",
        "양파",
        "삼겹살",
        "우유",
        "간장",
        "라면",
        "새우",
        "김치",
    ]

    print("\nmanual prediction check")
    for sample in test_samples:
        vector = np.expand_dims(build_vector(sample, vocab), axis=0)
        pred = model.predict(vector, verbose=0)[0]
        idx = int(np.argmax(pred))
        print(f"{sample} -> {labels[idx]} / confidence {pred[idx]:.4f}")

if __name__ == "__main__":
    main()
