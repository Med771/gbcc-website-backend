package backend.website.gbcc.logic.product;

import backend.website.gbcc.logic.product.dto.ProductSearchRequestDto;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public final class ProductSpecification {

    private ProductSpecification() {
    }

    public static Specification<ProductEntity> sameCategoryExcluding(UUID classId, UUID excludeProductId) {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get("productClass").get("id"), classId),
                cb.notEqual(root.get("id"), excludeProductId)
        );
    }

    public static Specification<ProductEntity> activeProductsOnly() {
        return (root, query, cb) -> cb.equal(root.get("isActive"), true);
    }

    public static Specification<ProductEntity> byFilter(ProductSearchRequestDto filter) {
        return Specification.allOf(
                textQuery(filter.query()),
                classNameLike(filter.className()),
                classIdEquals(filter.classId()),
                seriesFilter(filter),
                brandLike(filter.brand()),
                minPrice(filter.minPrice()),
                maxPrice(filter.maxPrice()),
                heightRange(filter.minHeightMm(), filter.maxHeightMm()),
                isActive(filter.isActive())
        );
    }

    private static Specification<ProductEntity> classIdEquals(UUID classId) {
        return (root, query, cb) -> classId == null ? null : cb.equal(root.get("productClass").get("id"), classId);
    }

    private static Specification<ProductEntity> seriesFilter(ProductSearchRequestDto filter) {
        List<UUID> ids = filter.seriesIds();
        if (ids != null && !ids.isEmpty()) {
            return (root, q, cb) -> {
                var join = root.join("productSeries", JoinType.INNER);
                return join.get("id").in(ids);
            };
        }
        return seriesNameLike(filter.seriesName());
    }

    private static Specification<ProductEntity> textQuery(String query) {
        return (root, q, cb) -> {
            String normalized = normalize(query);
            if (normalized == null) {
                return null;
            }
            String safe = sanitizeForLikeSubstring(normalized);
            if (safe.isEmpty()) {
                return null;
            }
            String pattern = "%" + safe + "%";

            var brandPred = cb.like(cb.lower(root.get("brand")), pattern);

            var descPred = cb.and(
                    cb.isNotNull(root.get("description")),
                    cb.like(cb.lower(root.get("description")), pattern)
            );

            var classPred = cb.like(cb.lower(root.get("productClass").get("name")), pattern);

            var seriesJoin = root.join("productSeries", JoinType.LEFT);
            var seriesPred = cb.and(
                    cb.isNotNull(root.get("productSeries")),
                    cb.like(cb.lower(seriesJoin.get("name")), pattern)
            );

            return cb.or(brandPred, descPred, classPred, seriesPred);
        };
    }

    private static String sanitizeForLikeSubstring(String normalizedLowercase) {
        return normalizedLowercase
                .replace("\\", "")
                .replace("%", "")
                .replace("_", "");
    }

    private static Specification<ProductEntity> classNameLike(String className) {
        return (root, query, cb) -> {
            String normalized = normalize(className);
            if (normalized == null) {
                return null;
            }
            return cb.like(
                    cb.lower(root.get("productClass").get("name")),
                    normalized + "%"
            );
        };
    }

    private static Specification<ProductEntity> seriesNameLike(String seriesName) {
        return (root, query, cb) -> {
            String normalized = normalize(seriesName);
            if (normalized == null) {
                return null;
            }
            return cb.like(
                    cb.lower(root.get("productSeries").get("name")),
                    normalized + "%"
            );
        };
    }

    private static Specification<ProductEntity> brandLike(String brand) {
        return (root, query, cb) -> {
            String normalized = normalize(brand);
            if (normalized == null) {
                return null;
            }
            return cb.like(
                    cb.lower(root.get("brand")),
                    normalized + "%"
            );
        };
    }

    private static Specification<ProductEntity> minPrice(BigDecimal minPrice) {
        return (root, query, cb) -> minPrice == null ? null : cb.greaterThanOrEqualTo(root.get("price"), minPrice);
    }

    private static Specification<ProductEntity> maxPrice(BigDecimal maxPrice) {
        return (root, query, cb) -> maxPrice == null ? null : cb.lessThanOrEqualTo(root.get("price"), maxPrice);
    }

    @SuppressWarnings("unchecked")
    private static Expression<Integer> heightMmPath(Root<ProductEntity> root) {
        return (Expression<Integer>) (Expression<?>) root.get("heightMm");
    }

    private static Specification<ProductEntity> heightRange(Integer minHeightMm, Integer maxHeightMm) {
        return (root, query, cb) -> {
            if (minHeightMm == null && maxHeightMm == null) {
                return null;
            }
            Expression<Integer> height = heightMmPath(root);
            if (minHeightMm != null && maxHeightMm != null) {
                return cb.between(height, minHeightMm, maxHeightMm);
            }
            if (minHeightMm != null) {
                return cb.greaterThanOrEqualTo(height, minHeightMm);
            }
            return cb.lessThanOrEqualTo(height, maxHeightMm);
        };
    }

    private static Specification<ProductEntity> isActive(Boolean isActive) {
        return (root, query, cb) -> isActive == null ? null : cb.equal(root.get("isActive"), isActive);
    }

    private static String normalize(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim().toLowerCase();
    }
}
