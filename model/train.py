import re

import joblib
import pandas as pd
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import accuracy_score, classification_report
from sklearn.model_selection import train_test_split
from sklearn.pipeline import Pipeline

DATA = "model/DataOhrid_FINAL.xlsx"
OUT = "sentiment-service/model.joblib"


def to_sentiment(rating):
    if rating >= 4:
        return "positive"
    if rating == 3:
        return "neutral"
    return "negative"


def clean_text(text):
    text = str(text).lower()
    text = re.sub(r"[^a-z\s]", " ", text)
    return re.sub(r"\s+", " ", text).strip()


def main():
    df = pd.read_excel(DATA)
    df["sentiment"] = df["rating"].apply(to_sentiment)
    df["clean_text"] = df["review_text"].apply(clean_text)

    X_train, X_test, y_train, y_test = train_test_split(
        df["clean_text"],
        df["sentiment"],
        test_size=0.2,
        random_state=42,
        stratify=df["sentiment"],
    )

    model = Pipeline(
        [
            ("tfidf", TfidfVectorizer(max_features=5000)),
            ("clf", LogisticRegression(max_iter=1000, class_weight="balanced")),
        ]
    )
    model.fit(X_train, y_train)

    preds = model.predict(X_test)
    print("Test accuracy:", round(accuracy_score(y_test, preds), 3))
    print()
    print(classification_report(y_test, preds, zero_division=0))

    joblib.dump(model, OUT)
    print("Saved model to", OUT)


if __name__ == "__main__":
    main()
