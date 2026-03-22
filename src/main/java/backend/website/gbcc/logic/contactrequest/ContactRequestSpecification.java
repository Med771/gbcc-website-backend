package backend.website.gbcc.logic.contactrequest;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class ContactRequestSpecification {

    private ContactRequestSpecification() {
    }

    public static Specification<ContactRequestEntity> onlyUnassigned(Boolean onlyUnassigned) {
        return (root, query, cb) -> {
            if (!Boolean.TRUE.equals(onlyUnassigned)) {
                return null;
            }
            return cb.isNull(root.get("assignedTo"));
        };
    }

    public static Specification<ContactRequestEntity> queryLike(String query) {
        return (root, q, cb) -> {
            if (!StringUtils.hasText(query)) {
                return null;
            }
            String normalized = "%" + query.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), normalized),
                    cb.like(cb.lower(root.get("email")), normalized),
                    cb.like(cb.lower(root.get("message")), normalized),
                    cb.like(cb.lower(root.get("phone")), normalized)
            );
        };
    }
}
