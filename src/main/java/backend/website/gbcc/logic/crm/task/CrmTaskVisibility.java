package backend.website.gbcc.logic.crm.task;

import backend.website.gbcc.model.AccountPrincipal;
import backend.website.gbcc.model.AccountRole;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public final class CrmTaskVisibility {

    private CrmTaskVisibility() {
    }

    public static Specification<CrmTaskEntity> visibleFor(AccountPrincipal principal) {
        return (root, query, cb) -> {
            if (principal.role() == AccountRole.OWNER) {
                return cb.conjunction();
            }
            Join<Object, Object> assignee = root.join("assignee", JoinType.INNER);
            return cb.equal(assignee.get("id"), principal.accountId());
        };
    }
}
