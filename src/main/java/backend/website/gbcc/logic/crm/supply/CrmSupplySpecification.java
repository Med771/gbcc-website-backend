package backend.website.gbcc.logic.crm.supply;

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.UUID;

public final class CrmSupplySpecification {

    private CrmSupplySpecification() {
    }

    public static Specification<CrmSupplyEntity> organizationIdEq(UUID id) {
        return (root, q, cb) -> id == null ? cb.conjunction() : cb.equal(root.get("organization").get("id"), id);
    }

    public static Specification<CrmSupplyEntity> statusEq(CrmSupplyStatus status) {
        return (root, q, cb) -> status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }

    public static Specification<CrmSupplyEntity> supplyAtBetween(LocalDate from, LocalDate to) {
        return (root, q, cb) -> {
            if (from != null && to != null) {
                return cb.between(root.get("supplyAt"), from, to);
            }
            if (from != null) {
                return cb.greaterThanOrEqualTo(root.get("supplyAt"), from);
            }
            if (to != null) {
                return cb.lessThanOrEqualTo(root.get("supplyAt"), to);
            }
            return cb.conjunction();
        };
    }
}
