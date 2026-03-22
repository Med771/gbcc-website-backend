package backend.website.gbcc.logic.cooperationrequest;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class CooperationRequestSpecification {

    private CooperationRequestSpecification() {
    }

    public static Specification<CooperationRequestEntity> onlyUnassigned(Boolean onlyUnassigned) {
        return (root, query, cb) -> {
            if (!Boolean.TRUE.equals(onlyUnassigned)) {
                return null;
            }
            return cb.isNull(root.get("assignedTo"));
        };
    }

    public static Specification<CooperationRequestEntity> queryLike(String query) {
        return (root, q, cb) -> {
            if (!StringUtils.hasText(query)) {
                return null;
            }
            String normalized = "%" + query.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), normalized),
                    cb.like(cb.lower(root.get("email")), normalized),
                    cb.like(cb.lower(root.get("phone")), normalized),
                    cb.like(cb.lower(root.get("comment")), normalized),
                    cb.like(cb.lower(root.get("cooperationType")), normalized)
            );
        };
    }
}
