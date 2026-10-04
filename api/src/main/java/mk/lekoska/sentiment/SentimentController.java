package mk.lekoska.sentiment;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reviews")
public class SentimentController {

    private final SentimentClient sentimentClient;

    public SentimentController(SentimentClient sentimentClient) {
        this.sentimentClient = sentimentClient;
    }

    @PostMapping("/analyse")
    public SentimentResponse analyse(@Valid @RequestBody ReviewRequest request) {
        return sentimentClient.analyse(request.text());
    }
}
