package mk.lekoska.sentiment;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SentimentController.class)
class SentimentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SentimentClient sentimentClient;

    @Test
    void returnsSentimentForValidReview() throws Exception {
        given(sentimentClient.analyse(anyString()))
                .willReturn(new SentimentResponse("positive", 0.9,
                        Map.of("negative", 0.05, "neutral", 0.05, "positive", 0.9)));

        mockMvc.perform(post("/api/v1/reviews/analyse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"a lovely place by the lake\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sentiment").value("positive"))
                .andExpect(jsonPath("$.confidence").value(0.9));
    }

    @Test
    void rejectsEmptyReview() throws Exception {
        mockMvc.perform(post("/api/v1/reviews/analyse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsServiceUnavailableWhenModelServiceIsDown() throws Exception {
        given(sentimentClient.analyse(anyString()))
                .willThrow(new SentimentServiceUnavailableException("down", null));

        mockMvc.perform(post("/api/v1/reviews/analyse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"a lovely place\"}"))
                .andExpect(status().isServiceUnavailable());
    }
}
