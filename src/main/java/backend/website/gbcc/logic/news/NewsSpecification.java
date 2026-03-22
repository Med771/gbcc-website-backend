package backend.website.gbcc.logic.news;

import backend.website.gbcc.logic.news.dto.NewsSearchRequestDto;
import backend.website.gbcc.model.NewsCategory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.Instant;

public final class NewsSpecification {

    private NewsSpecification() {
    }

    public static Specification<NewsEntity> byFilter(NewsSearchRequestDto filter) {
        return Specification.allOf(
                queryLike(filter.query()),
                isPublished(filter.isPublished()),
                categoryEqual(filter.category())
        );
    }

    private static Specification<NewsEntity> categoryEqual(NewsCategory category) {
        return (root, query, cb) -> category == null ? null : cb.equal(root.get("category"), category);
    }

    public static Specification<NewsEntity> visibleForPublic() {
        return Specification.allOf(
                isPublished(true),
                publishedAlready()
        );
    }

    private static Specification<NewsEntity> queryLike(String query) {
        return (root, q, cb) -> {
            if (!StringUtils.hasText(query)) {
                return null;
            }
            String normalized = "%" + query.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("title")), normalized),
                    cb.like(cb.lower(root.get("previewText")), normalized),
                    cb.like(cb.lower(root.get("content")), normalized)
            );
        };
    }

    private static Specification<NewsEntity> isPublished(Boolean isPublished) {
        return (root, query, cb) -> isPublished == null ? null : cb.equal(root.get("isPublished"), isPublished);
    }

    private static Specification<NewsEntity> publishedAlready() {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("publishedAt"), Instant.now());
    }
}
