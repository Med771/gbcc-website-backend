package backend.website.gbcc.logic.order;

import backend.website.gbcc.config.OpenApiConstants;
import backend.website.gbcc.logic.order.dto.CreateOrderRequestDto;
import backend.website.gbcc.logic.order.dto.OrderResponseDto;
import backend.website.gbcc.logic.order.dto.OrderSearchRequestDto;
import backend.website.gbcc.logic.order.dto.UpdateOrderStatusRequestDto;
import backend.website.gbcc.model.OrderStatus;
import backend.website.gbcc.model.dto.PageResponse;
import backend.website.gbcc.model.error.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
@Validated
@Tag(name = "Orders", description = """
        Заказы: оформляет только CUSTOMER (JWT). История заказов — GET /order (пагинация), карточка — GET /order/{id}.
        В ответе: displayNumber (короткий номер для UI), подписи статуса и способа оплаты, окно доставки, ссылка на чек.
        Просмотр: клиент — свои заказы; ADMIN/OWNER — любые. Смена статуса — только менеджеры.
        Цены и скидки фиксируются на момент заказа (товар + акции).
        """)
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Создать заказ", description = """
            Требует JWT роли CUSTOMER. Позиции: productId + quantity. Адрес доставки и способ оплаты обязательны.
            """)
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Заказ создан",
                    content = @Content(schema = @Schema(implementation = OrderResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Пустые позиции, неактивный товар и т.д.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Нет JWT",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Не CUSTOMER",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponseDto create(@Valid @RequestBody CreateOrderRequestDto requestDto) {
        return orderService.create(requestDto);
    }

    @Operation(summary = "Получить заказ по id", description = "JWT. Клиент — только свой заказ; менеджеры — любой.")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Найден",
                    content = @Content(schema = @Schema(implementation = OrderResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Нет JWT",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Нет доступа",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Не найден",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{orderId}")
    public OrderResponseDto getById(@PathVariable UUID orderId) {
        return orderService.getById(orderId);
    }

    @Operation(
            summary = "История / поиск заказов",
            description = """
                    JWT. Для CUSTOMER — только свои заказы (customerId подставляется автоматически): удобно для экрана «История заказов».
                    Менеджеры могут фильтровать по customerId и status.
                    """
    )
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @ApiResponse(responseCode = "200", description = "Страница заказов",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @GetMapping
    public PageResponse<OrderResponseDto> search(
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) OrderStatus status,
            @Parameter(hidden = true) Pageable pageable
    ) {
        OrderSearchRequestDto requestDto = new OrderSearchRequestDto(customerId, status);
        Page<OrderResponseDto> result = orderService.search(requestDto, pageable);
        return PageResponse.fromPage(result);
    }

    @Operation(summary = "Изменить статус заказа", description = """
            JWT. Только ADMIN/OWNER. Переходы статусов валидируются на сервере.
            Опционально: estimatedDeliveryAt / estimatedDeliveryEnd (окно доставки), receiptUrl (электронный чек), comment.
            """)
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Обновлено",
                    content = @Content(schema = @Schema(implementation = OrderResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Недопустимый переход статуса",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Нет JWT",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Не менеджер",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Заказ не найден",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PatchMapping("/{orderId}/status")
    public OrderResponseDto updateStatus(
            @PathVariable UUID orderId,
            @Valid @RequestBody UpdateOrderStatusRequestDto requestDto
    ) {
        return orderService.updateStatus(orderId, requestDto);
    }
}
