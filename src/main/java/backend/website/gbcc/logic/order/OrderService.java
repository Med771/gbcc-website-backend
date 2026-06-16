package backend.website.gbcc.logic.order;

import backend.website.gbcc.logic.order.dto.CreateOrderRequestDto;
import backend.website.gbcc.logic.order.dto.OrderResponseDto;
import backend.website.gbcc.logic.order.dto.OrderSearchRequestDto;
import backend.website.gbcc.logic.order.dto.PatchOrderManagerRequestDto;
import backend.website.gbcc.logic.order.dto.UpdateOrderStatusRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface OrderService {
    OrderResponseDto create(CreateOrderRequestDto requestDto);

    OrderResponseDto getById(UUID orderId);

    Page<OrderResponseDto> search(OrderSearchRequestDto requestDto, Pageable pageable);

    OrderResponseDto updateStatus(UUID orderId, UpdateOrderStatusRequestDto requestDto);

    OrderResponseDto patchByManager(UUID orderId, PatchOrderManagerRequestDto requestDto);
}
