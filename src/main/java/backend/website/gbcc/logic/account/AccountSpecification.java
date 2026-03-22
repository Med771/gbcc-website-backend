package backend.website.gbcc.logic.account;

import backend.website.gbcc.logic.account.dto.AccountSearchRequestDto;
import backend.website.gbcc.model.AccountRole;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class AccountSpecification {

    private AccountSpecification() {
    }

    public static Specification<AccountEntity> byFilter(AccountSearchRequestDto filter) {
        return Specification.allOf(
                nameLike(filter.name()),
                phoneLike(filter.phone()),
                emailLike(filter.email()),
                roleEquals(filter.role()),
                isBlockedEquals(filter.isBlocked())
        );
    }

    private static Specification<AccountEntity> nameLike(String name) {
        return (root, query, cb) -> {
            String normalized = normalize(name);
            if (normalized == null) {
                return null;
            }
            return cb.like(cb.lower(root.get("name")), normalized + "%");
        };
    }

    private static Specification<AccountEntity> phoneLike(String phone) {
        return (root, query, cb) -> {
            String normalized = normalize(phone);
            if (normalized == null) {
                return null;
            }
            return cb.like(cb.lower(root.get("phone")), normalized + "%");
        };
    }

    private static Specification<AccountEntity> emailLike(String email) {
        return (root, query, cb) -> {
            String normalized = normalize(email);
            if (normalized == null) {
                return null;
            }
            return cb.like(cb.lower(root.get("email")), normalized + "%");
        };
    }

    private static Specification<AccountEntity> roleEquals(AccountRole role) {
        return (root, query, cb) -> role == null ? null : cb.equal(root.get("role"), role);
    }

    private static Specification<AccountEntity> isBlockedEquals(Boolean isBlocked) {
        return (root, query, cb) -> isBlocked == null ? null : cb.equal(root.get("isBlocked"), isBlocked);
    }

    private static String normalize(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim().toLowerCase();
    }
}
