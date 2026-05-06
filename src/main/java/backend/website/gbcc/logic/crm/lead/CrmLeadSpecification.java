package backend.website.gbcc.logic.crm.lead;

import backend.website.gbcc.model.AccountPrincipal;
import backend.website.gbcc.model.AccountRole;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class CrmLeadSpecification {

    private CrmLeadSpecification() {
    }

    public static Specification<CrmLeadEntity> accessibleBy(AccountPrincipal principal) {
        return (root, query, cb) -> {
            if (principal.role() == AccountRole.OWNER) {
                return cb.conjunction();
            }
            Join<CrmLeadEntity, backend.website.gbcc.logic.account.AccountEntity> assigned =
                    root.join("assignedTo", JoinType.INNER);
            return cb.equal(assigned.get("id"), principal.accountId());
        };
    }

    public static Specification<CrmLeadEntity> queryLike(String q) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(q)) {
                return cb.conjunction();
            }
            String pattern = "%" + q.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("companyName")), pattern),
                    cb.like(cb.lower(cb.coalesce(root.get("phones"), "")), pattern),
                    cb.like(cb.lower(cb.coalesce(root.get("managerComment"), "")), pattern)
            );
        };
    }
}
