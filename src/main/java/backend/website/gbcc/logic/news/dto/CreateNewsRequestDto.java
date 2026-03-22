package backend.website.gbcc.logic.news.dto;

import backend.website.gbcc.model.NewsCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record CreateNewsRequestDto(
        @NotBlank(message = "title is required")
        String title,
        @NotBlank(message = "previewText is required")
        String previewText,
        @NotBlank(message = "content is required")
        String content,
        @NotNull(message = "category is required")
        NewsCategory category,
        UUID coverFileId,
        Boolean isPublished,
        Instant publishedAt
) {
}
