package backend.website.gbcc.logic.news.dto;

import backend.website.gbcc.model.NewsCategory;

public record NewsSearchRequestDto(
        String query,
        Boolean isPublished,
        NewsCategory category
) {
}
