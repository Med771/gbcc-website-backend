package backend.website.gbcc.logic.order;

import backend.website.gbcc.logic.order.dto.OrderItemResponseDto;
import backend.website.gbcc.logic.order.dto.OrderResponseDto;
import backend.website.gbcc.logic.order.dto.OrderStatusHistoryResponseDto;
import backend.website.gbcc.logic.product.ProductEntity;
import backend.website.gbcc.model.OrderPaymentMethodLabels;
import backend.website.gbcc.model.OrderStatusLabels;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderMapper {

    public OrderResponseDto toResponse(
            OrderEntity order,
            List<OrderItemEntity> items,
            List<OrderStatusHistoryEntity> history
    ) {
        return new OrderResponseDto(
                order.getId(),
                order.getDisplayNumber(),
                order.getCustomer().getId(),
                order.getStatus(),
                OrderStatusLabels.ru(order.getStatus()),
                order.getDeliveryAddress(),
                order.getCustomerComment(),
                order.getPaymentMethod(),
                OrderPaymentMethodLabels.ru(order.getPaymentMethod()),
                order.getReceiptUrl(),
                order.getEstimatedDeliveryAt(),
                order.getEstimatedDeliveryEnd(),
                order.getTotalPrice(),
                order.getTotalDiscountedPrice(),
                items.stream().map(this::toItemResponse).toList(),
                history.stream().map(this::toHistoryResponse).toList(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    private OrderItemResponseDto toItemResponse(OrderItemEntity item) {
        ProductEntity p = item.getProduct();
        return new OrderItemResponseDto(
                item.getId(),
                p.getId(),
                p.getBrand(),
                p.getProductClass() != null ? p.getProductClass().getName() : null,
                p.getProductSeries() != null ? p.getProductSeries().getName() : null,
                p.getProductType() != null ? p.getProductType().getName() : null,
                item.getQuantity(),
                item.getUnitPrice(),
                item.getUnitDiscountPercent(),
                item.getUnitDiscountedPrice(),
                item.getLineTotalPrice(),
                item.getLineTotalDiscountedPrice()
        );
    }

    private OrderStatusHistoryResponseDto toHistoryResponse(OrderStatusHistoryEntity history) {
        return new OrderStatusHistoryResponseDto(
                history.getId(),
                history.getFromStatus(),
                history.getToStatus(),
                history.getChangedByAccount() != null ? history.getChangedByAccount().getId() : null,
                history.getComment(),
                history.getEstimatedDeliveryAt(),
                history.getCreatedAt()
        );
    }
}
