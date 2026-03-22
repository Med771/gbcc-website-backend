package backend.website.gbcc.logic.order;

import backend.website.gbcc.logic.order.dto.OrderSearchRequestDto;
import backend.website.gbcc.model.OrderStatus;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public final class OrderSpecification {

    private OrderSpecification() {
    }

    public static Specification<OrderEntity> byFilter(OrderSearchRequestDto filter) {
        return Specification.allOf(
                customerId(filter.customerId()),
                status(filter.status())
        );
    }

    private static Specification<OrderEntity> customerId(UUID customerId) {
        return (root, query, cb) -> customerId == null ? null : cb.equal(root.get("customer").get("id"), customerId);
    }

    private static Specification<OrderEntity> status(OrderStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }
}
