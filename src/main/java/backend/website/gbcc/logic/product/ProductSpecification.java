package backend.website.gbcc.logic.product;

import backend.website.gbcc.logic.product.dto.ProductSearchRequestDto;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

public final class ProductSpecification {

    private ProductSpecification() {
    }

    public static Specification<ProductEntity> byFilter(ProductSearchRequestDto filter) {
        return Specification.allOf(
                classNameLike(filter.className()),
                seriesNameLike(filter.seriesName()),
                typeNameLike(filter.typeName()),
                brandLike(filter.brand()),
                minPrice(filter.minPrice()),
                maxPrice(filter.maxPrice()),
                isActive(filter.isActive())
        );
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

    private static Specification<ProductEntity> typeNameLike(String typeName) {
        return (root, query, cb) -> {
            String normalized = normalize(typeName);
            if (normalized == null) {
                return null;
            }
            return cb.like(
                    cb.lower(root.get("productType").get("name")),
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
