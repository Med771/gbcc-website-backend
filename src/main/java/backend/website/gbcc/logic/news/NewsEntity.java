package backend.website.gbcc.logic.news;

import backend.website.gbcc.model.BaseEntity;
import backend.website.gbcc.model.NewsCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "news")
@Getter
@Setter
@NoArgsConstructor
public class NewsEntity extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(name = "preview_text", nullable = false, columnDefinition = "text")
    private String previewText;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Column(name = "cover_file_id")
    private UUID coverFileId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private NewsCategory category = NewsCategory.PRODUCTS;

    @Column(name = "is_published", nullable = false)
    private Boolean isPublished = false;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "created_by_account_id", nullable = false)
    private UUID createdByAccountId;

    @Column(name = "updated_by_account_id", nullable = false)
    private UUID updatedByAccountId;
}
