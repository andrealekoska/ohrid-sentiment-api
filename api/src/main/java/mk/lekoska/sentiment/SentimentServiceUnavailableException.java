package mk.lekoska.sentiment;

public class SentimentServiceUnavailableException extends RuntimeException {

    public SentimentServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
