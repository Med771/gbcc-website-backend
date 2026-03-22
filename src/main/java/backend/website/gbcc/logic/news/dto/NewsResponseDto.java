package backend.website.gbcc.logic.news.dto;

import backend.website.gbcc.model.NewsCategory;

import java.time.Instant;
import java.util.UUID;

public record NewsResponseDto(
        UUID id,
        String title,
        String previewText,
        String content,
        UUID coverFileId,
        NewsCategory category,
        Boolean isPublished,
        Instant publishedAt,
        UUID createdByAccountId,
        UUID updatedByAccountId,
        Instant createdAt,
        Instant updatedAt
) {
}
