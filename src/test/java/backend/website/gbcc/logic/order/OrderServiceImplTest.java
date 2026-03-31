package backend.website.gbcc.logic.order;

import backend.website.gbcc.helper.SecurityContextHelper;
import backend.website.gbcc.logic.account.AccountEntity;
import backend.website.gbcc.logic.account.AccountRepository;
import backend.website.gbcc.logic.order.dto.CreateOrderItemRequestDto;
import backend.website.gbcc.logic.order.dto.CreateOrderRequestDto;
import backend.website.gbcc.logic.order.dto.OrderResponseDto;
import backend.website.gbcc.logic.order.dto.OrderSearchRequestDto;
import backend.website.gbcc.logic.order.dto.UpdateOrderStatusRequestDto;
import backend.website.gbcc.logic.product.ProductEntity;
import backend.website.gbcc.logic.product.ProductRepository;
import backend.website.gbcc.logic.promotion.PromotionDiscountResolver;
import backend.website.gbcc.logic.referral.ReferralService;
import backend.website.gbcc.model.AccountRole;
import backend.website.gbcc.model.OrderPaymentMethod;
import backend.website.gbcc.model.OrderStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private OrderStatusHistoryRepository orderStatusHistoryRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private SecurityContextHelper securityContextHelper;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private PromotionDiscountResolver promotionDiscountResolver;
    @Mock
    private ReferralService referralService;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void create_shouldCreateOrderWithDiscountSnapshot() {
        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        AccountEntity customer = new AccountEntity();
        customer.setId(customerId);
        customer.setRole(AccountRole.CUSTOMER);

        ProductEntity product = new ProductEntity();
        product.setId(productId);
        product.setBrand("Brand A");
        product.setPrice(new BigDecimal("100.00"));
        product.setDiscountPercent(new BigDecimal("10.00"));
        product.setIsActive(true);

        OrderEntity persistedOrder = new OrderEntity();
        persistedOrder.setId(orderId);
        persistedOrder.setCustomer(customer);
        persistedOrder.setStatus(OrderStatus.CREATED);
        persistedOrder.setDeliveryAddress("Street 1");
        persistedOrder.setTotalPrice(new BigDecimal("200.00"));
        persistedOrder.setTotalDiscountedPrice(new BigDecimal("180.00"));

        when(securityContextHelper.getCurrentAccountIdOrThrow()).thenReturn(customerId);
        when(accountRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(orderRepository.nextDisplayNumber()).thenReturn(999_999L);
        when(productRepository.findAllWithTaxonomyByIds(any())).thenReturn(List.of(product));
        when(promotionDiscountResolver.resolveEffectiveDiscountPercent(any(), any(), any()))
                .thenAnswer(inv -> inv.getArgument(1));
        when(orderRepository.save(any(OrderEntity.class))).thenReturn(persistedOrder);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(persistedOrder));
        when(orderItemRepository.findAllByOrderId(orderId)).thenReturn(Collections.emptyList());
        when(orderStatusHistoryRepository.findAllByOrderId(orderId)).thenReturn(Collections.emptyList());
        when(orderMapper.toResponse(any(), any(), any())).thenReturn(new OrderResponseDto(
                orderId,
                999_999L,
                customerId,
                OrderStatus.CREATED,
                "Оформлен",
                "Street 1",
                null,
                OrderPaymentMethod.CARD_OR_ON_RECEIPT,
                "Картой / при получении",
                null,
                null,
                null,
                new BigDecimal("200.00"),
                new BigDecimal("180.00"),
                Collections.emptyList(),
                Collections.emptyList(),
                null,
                null
        ));

        CreateOrderRequestDto request = new CreateOrderRequestDto(
                "Street 1",
                null,
                OrderPaymentMethod.CARD_OR_ON_RECEIPT,
                List.of(new CreateOrderItemRequestDto(productId, 2))
        );

        OrderResponseDto response = orderService.create(request);

        assertThat(response.totalPrice()).isEqualByComparingTo("200.00");
        assertThat(response.totalDiscountedPrice()).isEqualByComparingTo("180.00");
        verify(orderItemRepository).save(any(OrderItemEntity.class));
        verify(orderStatusHistoryRepository).save(any(OrderStatusHistoryEntity.class));
    }

    @Test
    void updateStatus_shouldThrowForbidden_forCustomer() {
        UUID customerId = UUID.randomUUID();
        AccountEntity customer = new AccountEntity();
        customer.setId(customerId);
        customer.setRole(AccountRole.CUSTOMER);

        when(securityContextHelper.getCurrentAccountIdOrThrow()).thenReturn(customerId);
        when(accountRepository.findById(customerId)).thenReturn(Optional.of(customer));

        UpdateOrderStatusRequestDto request = new UpdateOrderStatusRequestDto(
                OrderStatus.PROCESSING,
                null,
                null,
                null,
                "accepted"
        );

        assertThatThrownBy(() -> orderService.updateStatus(UUID.randomUUID(), request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> {
                    ResponseStatusException ex = (ResponseStatusException) error;
                    assertThat(ex.getStatusCode().value()).isEqualTo(FORBIDDEN.value());
                });
    }

    @Test
    void updateStatus_shouldThrowBadRequest_forInvalidTransition() {
        UUID adminId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        AccountEntity admin = new AccountEntity();
        admin.setId(adminId);
        admin.setRole(AccountRole.ADMIN);

        AccountEntity customer = new AccountEntity();
        customer.setId(UUID.randomUUID());
        customer.setRole(AccountRole.CUSTOMER);

        OrderEntity order = new OrderEntity();
        order.setId(orderId);
        order.setCustomer(customer);
        order.setStatus(OrderStatus.CREATED);

        when(securityContextHelper.getCurrentAccountIdOrThrow()).thenReturn(adminId);
        when(accountRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        UpdateOrderStatusRequestDto request = new UpdateOrderStatusRequestDto(
                OrderStatus.SHIPPED,
                null,
                null,
                null,
                "skip steps"
        );

        assertThatThrownBy(() -> orderService.updateStatus(orderId, request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> {
                    ResponseStatusException ex = (ResponseStatusException) error;
                    assertThat(ex.getStatusCode().value()).isEqualTo(BAD_REQUEST.value());
                });
    }

    @Test
    void search_shouldForceOwnOrders_forCustomer() {
        UUID customerId = UUID.randomUUID();

        AccountEntity customer = new AccountEntity();
        customer.setId(customerId);
        customer.setRole(AccountRole.CUSTOMER);

        when(securityContextHelper.getCurrentAccountIdOrThrow()).thenReturn(customerId);
        when(accountRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(orderRepository.findAll(org.mockito.ArgumentMatchers.<Specification<OrderEntity>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        orderService.search(new OrderSearchRequestDto(UUID.randomUUID(), OrderStatus.CREATED), PageRequest.of(0, 20));

        verify(orderRepository).findAll(org.mockito.ArgumentMatchers.<Specification<OrderEntity>>any(), any(Pageable.class));
    }
}
