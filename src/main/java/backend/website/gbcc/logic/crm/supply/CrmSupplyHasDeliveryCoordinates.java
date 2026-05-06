package backend.website.gbcc.logic.crm.supply;

import org.springframework.data.jpa.domain.Specification;

public final class CrmSupplyHasDeliveryCoordinates {

    private CrmSupplyHasDeliveryCoordinates() {
    }

    public static Specification<CrmSupplyEntity> hasDeliveryCoordinates() {
        return (root, q, cb) -> cb.and(
                cb.isNotNull(root.get("deliveryLatitude")),
                cb.isNotNull(root.get("deliveryLongitude"))
        );
    }
}
