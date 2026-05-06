package backend.website.gbcc.logic.crm.organization;

import org.springframework.data.jpa.domain.Specification;

public final class CrmOrganizationHasCoordinates {

    private CrmOrganizationHasCoordinates() {
    }

    public static Specification<CrmOrganizationEntity> hasCoordinates() {
        return (root, q, cb) -> cb.and(
                cb.isNotNull(root.get("latitude")),
                cb.isNotNull(root.get("longitude"))
        );
    }
}
