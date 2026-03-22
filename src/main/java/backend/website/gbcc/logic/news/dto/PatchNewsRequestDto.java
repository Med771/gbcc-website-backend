package backend.website.gbcc.logic.news.dto;

import backend.website.gbcc.model.NewsCategory;

import java.time.Instant;
import java.util.UUID;

public record PatchNewsRequestDto(
        String title,
        String previewText,
        String content,
        NewsCategory category,
        UUID coverFileId,
        Boolean isPublished,
        Instant publishedAt
) {
}
