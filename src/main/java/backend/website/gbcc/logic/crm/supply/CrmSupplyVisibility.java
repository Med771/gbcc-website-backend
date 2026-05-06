package backend.website.gbcc.logic.crm.supply;

import backend.website.gbcc.model.AccountPrincipal;
import backend.website.gbcc.model.AccountRole;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public final class CrmSupplyVisibility {

    private CrmSupplyVisibility() {
    }

    public static Specification<CrmSupplyEntity> visibleFor(AccountPrincipal principal) {
        return (root, query, cb) -> {
            if (principal.role() == AccountRole.OWNER) {
                return cb.conjunction();
            }
            Join<Object, Object> org = root.join("organization", JoinType.INNER);
            Join<Object, Object> assignee = org.join("assignedTo", JoinType.INNER);
            return cb.equal(assignee.get("id"), principal.accountId());
        };
    }
}
