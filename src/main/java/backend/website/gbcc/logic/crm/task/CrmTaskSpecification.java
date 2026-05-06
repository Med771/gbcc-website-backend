package backend.website.gbcc.logic.crm.task;

import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.UUID;

public final class CrmTaskSpecification {

    private CrmTaskSpecification() {
    }

    public static Specification<CrmTaskEntity> organizationIdEq(UUID id) {
        return (root, q, cb) -> id == null ? cb.conjunction() : cb.equal(root.get("organization").get("id"), id);
    }

    public static Specification<CrmTaskEntity> leadIdEq(UUID id) {
        return (root, q, cb) -> id == null ? cb.conjunction() : cb.equal(root.get("lead").get("id"), id);
    }

    public static Specification<CrmTaskEntity> statusEq(CrmTaskStatus status) {
        return (root, q, cb) -> status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }

    public static Specification<CrmTaskEntity> assigneeIdEq(UUID id) {
        return (root, q, cb) -> id == null ? cb.conjunction() : cb.equal(root.get("assignee").get("id"), id);
    }

    public static Specification<CrmTaskEntity> dueBefore(Instant t) {
        return (root, q, cb) -> t == null ? cb.conjunction() : cb.lessThanOrEqualTo(root.get("dueAt"), t);
    }

    public static Specification<CrmTaskEntity> dueAfter(Instant t) {
        return (root, q, cb) -> t == null ? cb.conjunction() : cb.greaterThanOrEqualTo(root.get("dueAt"), t);
    }
}
