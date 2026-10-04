package mk.lekoska.sentiment;

import java.util.Map;

public record SentimentResponse(String sentiment, double confidence, Map<String, Double> scores) {
}
