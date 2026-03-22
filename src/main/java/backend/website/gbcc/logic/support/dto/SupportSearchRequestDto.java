package backend.website.gbcc.logic.support.dto;

import backend.website.gbcc.model.SupportConversationStatus;

public record SupportSearchRequestDto(
        SupportConversationStatus status,
        String query
) {
}
