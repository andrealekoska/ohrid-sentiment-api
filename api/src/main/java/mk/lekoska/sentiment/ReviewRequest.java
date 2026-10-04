package mk.lekoska.sentiment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReviewRequest(
        @NotBlank(message = "Review text must not be empty")
        @Size(max = 5000, message = "Review text must not exceed 5000 characters")
        String text) {
}
