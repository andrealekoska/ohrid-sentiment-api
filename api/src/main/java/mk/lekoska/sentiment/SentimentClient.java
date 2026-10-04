package mk.lekoska.sentiment;

import java.net.http.HttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class SentimentClient {

    private final RestClient restClient;

    public SentimentClient(@Value("${sentiment.service.url}") String baseUrl) {
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(new JdkClientHttpRequestFactory(httpClient))
                .build();
    }
    public SentimentResponse analyse(String text) {
        try {
            return restClient.post()
                    .uri("/predict")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ReviewRequest(text))
                    .retrieve()
                    .body(SentimentResponse.class);
        } catch (RestClientException exception) {
            throw new SentimentServiceUnavailableException(
                    "The sentiment service is currently unavailable", exception);
        }
    }
}
