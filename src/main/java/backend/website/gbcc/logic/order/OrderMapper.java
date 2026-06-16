package backend.website.gbcc.logic.order;

import backend.website.gbcc.logic.order.dto.OrderItemResponseDto;
import backend.website.gbcc.logic.order.dto.OrderResponseDto;
import backend.website.gbcc.logic.order.dto.OrderStatusHistoryResponseDto;
import backend.website.gbcc.logic.product.ProductEntity;
import backend.website.gbcc.model.OrderPaymentMethodLabels;
import backend.website.gbcc.model.OrderStatusLabels;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class OrderMapper {

    public OrderResponseDto toResponse(
            OrderEntity order,
            List<OrderItemEntity> items,
            List<OrderStatusHistoryEntity> history
    ) {
        var customer = order.getCustomer();
        BigDecimal deliveryFee = order.getDeliveryFee() != null ? order.getDeliveryFee() : BigDecimal.ZERO;
        BigDecimal netTotal = order.getTotalDiscountedPrice().subtract(deliveryFee).setScale(2, RoundingMode.HALF_UP);

        return new OrderResponseDto(
                order.getId(),
                order.getDisplayNumber(),
                customer.getId(),
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
                deliveryFee,
                netTotal,
                resolveContactName(order, customer),
                resolveContactPhone(order, customer),
                resolveContactEmail(order, customer),
                order.getManagerNotes(),
                order.getCrmOrganization() != null ? order.getCrmOrganization().getId() : null,
                order.getCrmOrganization() != null ? order.getCrmOrganization().getName() : null,
                items.stream().map(this::toItemResponse).toList(),
                history.stream().map(this::toHistoryResponse).toList(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    private String resolveContactName(OrderEntity order, backend.website.gbcc.logic.account.AccountEntity customer) {
        if (StringUtils.hasText(order.getContactName())) {
            return order.getContactName();
        }
        return customer.getFirstName() + " " + customer.getLastName();
    }

    private String resolveContactPhone(OrderEntity order, backend.website.gbcc.logic.account.AccountEntity customer) {
        return StringUtils.hasText(order.getContactPhone()) ? order.getContactPhone() : customer.getPhone();
    }

    private String resolveContactEmail(OrderEntity order, backend.website.gbcc.logic.account.AccountEntity customer) {
        return StringUtils.hasText(order.getContactEmail()) ? order.getContactEmail() : customer.getEmail();
    }

    private OrderItemResponseDto toItemResponse(OrderItemEntity item) {
        ProductEntity p = item.getProduct();
        return new OrderItemResponseDto(
                item.getId(),
                p.getId(),
                p.getBrand(),
                p.getProductClass() != null ? p.getProductClass().getName() : null,
                p.getProductSeries() != null ? p.getProductSeries().getName() : null,
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
