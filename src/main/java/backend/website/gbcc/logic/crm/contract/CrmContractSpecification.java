package backend.website.gbcc.logic.crm.contract;

import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public final class CrmContractSpecification {

    private CrmContractSpecification() {
    }

    public static Specification<CrmContractEntity> organizationIdEq(UUID id) {
        return (root, q, cb) -> id == null ? cb.conjunction() : cb.equal(root.get("organization").get("id"), id);
    }
}
