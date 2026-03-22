package backend.website.gbcc.logic.support;

import backend.website.gbcc.logic.support.dto.SupportSearchRequestDto;
import backend.website.gbcc.model.SupportConversationStatus;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class SupportSpecification {

    private SupportSpecification() {
    }

    public static Specification<SupportConversationEntity> byFilter(SupportSearchRequestDto filter) {
        SupportSearchRequestDto safe = filter != null
                ? filter
                : new SupportSearchRequestDto(null, null);

        return Specification.allOf(
                statusEqual(safe.status()),
                queryLike(safe.query())
        );
    }

    private static Specification<SupportConversationEntity> statusEqual(SupportConversationStatus status) {
        return (root, q, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    private static Specification<SupportConversationEntity> queryLike(String query) {
        return (root, q, cb) -> {
            if (!StringUtils.hasText(query)) {
                return null;
            }
            String pattern = "%" + query.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("subject")), pattern),
                    cb.like(cb.lower(root.get("guestEmail")), pattern)
            );
        };
    }
}
