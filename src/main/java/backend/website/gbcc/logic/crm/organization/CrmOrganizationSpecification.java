package backend.website.gbcc.logic.crm.organization;

import backend.website.gbcc.model.AccountPrincipal;
import backend.website.gbcc.model.AccountRole;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.UUID;

public final class CrmOrganizationSpecification {

    private CrmOrganizationSpecification() {
    }

    public static Specification<CrmOrganizationEntity> accessibleBy(AccountPrincipal principal) {
        return (root, query, cb) -> {
            if (principal.role() == AccountRole.OWNER) {
                return cb.conjunction();
            }
            jakarta.persistence.criteria.Join<CrmOrganizationEntity, backend.website.gbcc.logic.account.AccountEntity> assigned =
                    root.join("assignedTo", jakarta.persistence.criteria.JoinType.INNER);
            return cb.equal(assigned.get("id"), principal.accountId());
        };
    }

    public static Specification<CrmOrganizationEntity> queryLike(String q) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(q)) {
                return cb.conjunction();
            }
            String pattern = "%" + q.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(cb.coalesce(root.get("externalNumber"), "")), pattern),
                    cb.like(cb.lower(cb.coalesce(root.get("legalAddress"), "")), pattern),
                    cb.like(cb.lower(cb.coalesce(root.get("deliveryAddress"), "")), pattern)
            );
        };
    }

    public static Specification<CrmOrganizationEntity> assignedTo(UUID accountId) {
        return (root, query, cb) -> {
            if (accountId == null) {
                return cb.isNull(root.get("assignedTo"));
            }
            Join<Object, Object> assigned = root.join("assignedTo", JoinType.INNER);
            return cb.equal(assigned.get("id"), accountId);
        };
    }
}
