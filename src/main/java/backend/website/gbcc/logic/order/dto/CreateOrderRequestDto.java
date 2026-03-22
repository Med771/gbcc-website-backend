package backend.website.gbcc.logic.order.dto;

import backend.website.gbcc.model.OrderPaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateOrderRequestDto(
        @NotBlank(message = "deliveryAddress is required")
        String deliveryAddress,
        String customerComment,
        @NotNull(message = "paymentMethod is required")
        OrderPaymentMethod paymentMethod,
        @NotEmpty(message = "items must not be empty")
        List<@Valid CreateOrderItemRequestDto> items
) {
}
