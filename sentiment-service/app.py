import os
import re

import joblib
from fastapi import FastAPI
from pydantic import BaseModel, Field

MODEL_PATH = os.getenv("MODEL_PATH", "model.joblib")

app = FastAPI(
    title="Ohrid Sentiment Service",
    description="Serves the sentiment model trained on Ohrid tourist reviews.",
    version="1.0.0",
)

model = joblib.load(MODEL_PATH)


class ReviewRequest(BaseModel):
    text: str = Field(min_length=1, max_length=5000)


class SentimentResponse(BaseModel):
    sentiment: str
    confidence: float
    scores: dict[str, float]


def clean_text(text: str) -> str:
    text = str(text).lower()
    text = re.sub(r"[^a-z\s]", " ", text)
    return re.sub(r"\s+", " ", text).strip()


@app.get("/health")
def health():
    return {"status": "ok"}


@app.post("/predict", response_model=SentimentResponse)
def predict(request: ReviewRequest):
    cleaned = clean_text(request.text)
    probabilities = model.predict_proba([cleaned])[0]
    labels = list(model.classes_)
    scores = {label: round(float(p), 3) for label, p in zip(labels, probabilities)}
    best = max(scores, key=scores.get)
    return SentimentResponse(sentiment=best, confidence=scores[best], scores=scores)
