package backend.website.gbcc.logic.crm.supply;

import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public final class CrmSupplySpecification {

    private CrmSupplySpecification() {
    }

    public static Specification<CrmSupplyEntity> organizationIdEq(UUID id) {
        return (root, q, cb) -> id == null ? cb.conjunction() : cb.equal(root.get("organization").get("id"), id);
    }
}
