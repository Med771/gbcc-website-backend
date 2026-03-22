package backend.website.gbcc.logic.news;

import backend.website.gbcc.logic.news.dto.NewsResponseDto;
import org.springframework.stereotype.Component;

@Component
public class NewsMapper {

    public NewsResponseDto toResponse(NewsEntity entity) {
        return new NewsResponseDto(
                entity.getId(),
                entity.getTitle(),
                entity.getPreviewText(),
                entity.getContent(),
                entity.getCoverFileId(),
                entity.getCategory(),
                entity.getIsPublished(),
                entity.getPublishedAt(),
                entity.getCreatedByAccountId(),
                entity.getUpdatedByAccountId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
