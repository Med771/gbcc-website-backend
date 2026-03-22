package backend.website.gbcc.logic.support.dto;

import backend.website.gbcc.model.SupportConversationStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateSupportConversationStatusRequestDto(
        @NotNull(message = "status is required")
        SupportConversationStatus status
) {
}
