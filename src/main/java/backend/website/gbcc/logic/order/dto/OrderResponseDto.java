package backend.website.gbcc.logic.order.dto;

import backend.website.gbcc.model.OrderPaymentMethod;
import backend.website.gbcc.model.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponseDto(
        UUID id,
        long displayNumber,
        UUID customerId,
        OrderStatus status,
        String statusLabel,
        String deliveryAddress,
        String customerComment,
        OrderPaymentMethod paymentMethod,
        String paymentMethodLabel,
        String receiptUrl,
        Instant estimatedDeliveryAt,
        Instant estimatedDeliveryEnd,
        BigDecimal totalPrice,
        BigDecimal totalDiscountedPrice,
        List<OrderItemResponseDto> items,
        List<OrderStatusHistoryResponseDto> history,
        Instant createdAt,
        Instant updatedAt
) {
}
