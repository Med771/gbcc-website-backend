package backend.website.gbcc.logic.crm.interaction;

import backend.website.gbcc.model.AccountPrincipal;
import backend.website.gbcc.model.AccountRole;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public final class CrmInteractionVisibility {

    private CrmInteractionVisibility() {
    }

    public static Specification<CrmInteractionEntity> visibleFor(AccountPrincipal principal) {
        return (root, query, cb) -> {
            if (principal.role() == AccountRole.OWNER) {
                return cb.conjunction();
            }
            Join<Object, Object> org = root.join("organization", JoinType.LEFT);
            Join<Object, Object> orgAssign = org.join("assignedTo", JoinType.LEFT);
            Join<Object, Object> lead = root.join("lead", JoinType.LEFT);
            Join<Object, Object> leadAssign = lead.join("assignedTo", JoinType.LEFT);
            return cb.or(
                    cb.equal(orgAssign.get("id"), principal.accountId()),
                    cb.equal(leadAssign.get("id"), principal.accountId())
            );
        };
    }
}
