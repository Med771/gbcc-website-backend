package backend.website.gbcc.logic.promotion;

import backend.website.gbcc.logic.promotion.dto.PromotionSearchRequestDto;
import backend.website.gbcc.model.PromotionScopeType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.Instant;

public final class PromotionSpecification {

    private PromotionSpecification() {
    }

    public static Specification<PromotionEntity> byFilter(PromotionSearchRequestDto filter) {
        return Specification.allOf(
                nameLike(filter != null ? filter.name() : null),
                isActive(filter != null ? filter.isActive() : null),
                scopeEqual(filter != null ? filter.scope() : null)
        );
    }

    /**
     * Active promotions whose validity window contains {@code at} (inclusive bounds).
     */
    public static Specification<PromotionEntity> visibleAt(Instant at) {
        return Specification.allOf(
                (root, q, cb) -> cb.isTrue(root.get("isActive")),
                (root, q, cb) -> cb.or(
                        cb.isNull(root.get("validFrom")),
                        cb.lessThanOrEqualTo(root.get("validFrom"), at)
                ),
                (root, q, cb) -> cb.or(
                        cb.isNull(root.get("validTo")),
                        cb.greaterThanOrEqualTo(root.get("validTo"), at)
                )
        );
    }

    private static Specification<PromotionEntity> nameLike(String name) {
        return (root, q, cb) -> {
            if (!StringUtils.hasText(name)) {
                return null;
            }
            String normalized = "%" + name.trim().toLowerCase() + "%";
            return cb.like(cb.lower(root.get("name")), normalized);
        };
    }

    private static Specification<PromotionEntity> isActive(Boolean isActive) {
        return (root, query, cb) -> isActive == null ? null : cb.equal(root.get("isActive"), isActive);
    }

    private static Specification<PromotionEntity> scopeEqual(PromotionScopeType scope) {
        return (root, query, cb) -> scope == null ? null : cb.equal(root.get("scope"), scope);
    }
}
