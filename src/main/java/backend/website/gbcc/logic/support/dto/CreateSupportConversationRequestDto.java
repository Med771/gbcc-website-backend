package backend.website.gbcc.logic.support.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSupportConversationRequestDto(
        @Size(max = 500, message = "subject must be at most 500 characters")
        String subject,
        @NotBlank(message = "message is required")
        @Size(max = 16000, message = "message must be at most 16000 characters")
        String message,
        @Size(max = 255, message = "guestName must be at most 255 characters")
        String guestName,
        @Email(message = "guestEmail must be a valid email")
        @Size(max = 255)
        String guestEmail
) {
}
