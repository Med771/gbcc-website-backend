package backend.website.gbcc.logic.support.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddSupportMessageRequestDto(
        @NotBlank(message = "message is required")
        @Size(max = 16000, message = "message must be at most 16000 characters")
        String message
) {
}
