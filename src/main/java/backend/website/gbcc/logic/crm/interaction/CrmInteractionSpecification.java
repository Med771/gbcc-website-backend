package backend.website.gbcc.logic.crm.interaction;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.UUID;

public final class CrmInteractionSpecification {

    private CrmInteractionSpecification() {
    }

    public static Specification<CrmInteractionEntity> organizationIdEq(UUID organizationId) {
        return (root, query, cb) -> organizationId == null ? cb.conjunction()
                : cb.equal(root.get("organization").get("id"), organizationId);
    }

    public static Specification<CrmInteractionEntity> leadIdEq(UUID leadId) {
        return (root, query, cb) -> leadId == null ? cb.conjunction()
                : cb.equal(root.get("lead").get("id"), leadId);
    }

    public static Specification<CrmInteractionEntity> authorIdEq(UUID authorId) {
        return (root, query, cb) -> authorId == null ? cb.conjunction()
                : cb.equal(root.get("author").get("id"), authorId);
    }

    public static Specification<CrmInteractionEntity> occurredBetween(Instant from, Instant to) {
        return (root, query, cb) -> {
            if (from != null && to != null) {
                return cb.between(root.get("occurredAt"), from, to);
            }
            if (from != null) {
                return cb.greaterThanOrEqualTo(root.get("occurredAt"), from);
            }
            if (to != null) {
                return cb.lessThanOrEqualTo(root.get("occurredAt"), to);
            }
            return cb.conjunction();
        };
    }

    public static Specification<CrmInteractionEntity> resultContains(String fragment) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(fragment)) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("resultNote")), "%" + fragment.trim().toLowerCase() + "%");
        };
    }

    public static Specification<CrmInteractionEntity> interactionTypeEq(CrmInteractionType type) {
        return (root, query, cb) -> type == null ? cb.conjunction()
                : cb.equal(root.get("interactionType"), type);
    }
}
