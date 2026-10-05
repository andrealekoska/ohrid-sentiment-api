# Ohrid Sentiment API

A REST API that classifies tourist reviews as positive, neutral or negative, using a model trained on reviews of Ohrid, North Macedonia.

The model comes from my own research, published as a full paper at ICT Innovations 2026 (Springer CCIS): *Comparative Analysis of Machine Learning Models for Sentiment Analysis of Tourist Reviews in Ohrid*. This repository takes that model out of a notebook and turns it into a service other software can call.

## How it works

Two services:

- **api** — Spring Boot (Java 21). The public REST API. Validates requests, calls the model service, and returns clean JSON, including clean errors.
- **sentiment-service** — FastAPI (Python). Loads the trained model and returns a prediction.

    client  →  api (8080)  →  sentiment-service (8000)  →  model

## Running it

Requires Docker.

    docker compose up --build

Then:

    curl -X POST http://localhost:8080/api/v1/reviews/analyse \
      -H "Content-Type: application/json" \
      -d '{"text": "The view over lake Ohrid was spectacular and the staff were very friendly"}'

Response:

    {
      "sentiment": "positive",
      "confidence": 0.837,
      "scores": { "negative": 0.079, "neutral": 0.084, "positive": 0.837 }
    }

## API

**POST /api/v1/reviews/analyse**

Request body: `{ "text": "a review, 1 to 5000 characters" }`

| Status | When |
| --- | --- |
| 200 | Prediction returned |
| 400 | Text missing, empty, or longer than 5000 characters |
| 503 | The model service is unreachable |

## The model

Trained by `model/train.py` on 1,060 TripAdvisor reviews collected for the paper, across fifteen locations and three domains: attractions, restaurants and hotels.

Ratings map to labels the same way as in the paper: 4–5 positive, 3 neutral, 1–2 negative. Text is lowercased and stripped of non-letter characters, then TF-IDF with at most 5,000 features feeds a Logistic Regression with balanced class weights, on an 80/20 split.

Test accuracy: **0.708**

| Class | Precision | Recall | F1 | Support |
| --- | --- | --- | --- | --- |
| negative | 0.70 | 0.77 | 0.73 | 43 |
| neutral | 0.52 | 0.48 | 0.50 | 56 |
| positive | 0.80 | 0.80 | 0.80 | 113 |

Neutral is the weakest class, which matches the paper: neutral tone is the hardest to separate and is the least represented in the data.

### Why not the transformer

The paper's best model was a fine-tuned RoBERTa at 0.76 accuracy. This service ships the Logistic Regression instead. It scores about five points lower, but the saved model is roughly 300 KB rather than 500 MB, it runs on CPU, and the container starts in seconds. For a service where the gap is this small, the smaller model was the better trade.

## Retraining

    python3 -m venv .venv
    source .venv/bin/activate
    pip install pandas scikit-learn openpyxl joblib
    python model/train.py

This rewrites `sentiment-service/model.joblib`.

## Tests

    cd api
    ./mvnw test

Three controller tests: a successful prediction, a rejected empty review, and the model service being down.

## Design notes

- The API reads the model service address from `SENTIMENT_SERVICE_URL`, so the same build runs locally and in Docker with no code change.
- The text cleaning in the service is identical to the cleaning used in training, to avoid training-serving skew.
- The Java image uses a multi-stage build, so the final image carries no Maven and no source.
- `pandas` is used by the training script only and is not installed in the service image.

## Author

Andrea Lekoska — [GitHub](https://github.com/andrealekoska) · [LinkedIn](https://linkedin.com/in/andrea-lekoska-7b54a317b) · [ORCID](https://orcid.org/0009-0007-1230-1221)
