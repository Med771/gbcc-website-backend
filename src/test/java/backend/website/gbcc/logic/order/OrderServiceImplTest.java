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
import backend.website.gbcc.logic.crm.access.CrmAccessPolicy;
import backend.website.gbcc.logic.crm.organization.CrmOrganizationRepository;
import backend.website.gbcc.logic.managercommission.ManagerCommissionService;
import backend.website.gbcc.logic.referral.ReferralService;
import backend.website.gbcc.model.AccountRole;
import backend.website.gbcc.model.OrderPaymentMethod;
import backend.website.gbcc.model.OrderStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

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
    @Mock
    private ManagerCommissionService managerCommissionService;
    @Mock
    private CrmOrganizationRepository crmOrganizationRepository;
    @Mock
    private CrmAccessPolicy crmAccessPolicy;

    @InjectMocks
    private OrderServiceImpl orderService;

    static Stream<Arguments> validStatusTransitions() {
        return Stream.of(
                Arguments.of(OrderStatus.CREATED, OrderStatus.PROCESSING, false),
                Arguments.of(OrderStatus.CREATED, OrderStatus.CANCELLED, false),
                Arguments.of(OrderStatus.PROCESSING, OrderStatus.PACKED, false),
                Arguments.of(OrderStatus.PROCESSING, OrderStatus.CANCELLED, false),
                Arguments.of(OrderStatus.PACKED, OrderStatus.SHIPPED, false),
                Arguments.of(OrderStatus.PACKED, OrderStatus.CANCELLED, false),
                Arguments.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED, false),
                Arguments.of(OrderStatus.SHIPPED, OrderStatus.DELIVERED, true)
        );
    }

    @ParameterizedTest(name = "{0} -> {1}")
    @MethodSource("validStatusTransitions")
    void updateStatus_validTransition_persistsHistoryAndReferralOnlyOnDelivered(
            OrderStatus from,
            OrderStatus to,
            boolean expectReferral
    ) {
        UUID managerId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        AccountEntity manager = new AccountEntity();
        manager.setId(managerId);
        manager.setRole(AccountRole.ADMIN);

        AccountEntity customer = new AccountEntity();
        customer.setId(customerId);
        customer.setRole(AccountRole.CUSTOMER);

        OrderEntity order = new OrderEntity();
        order.setId(orderId);
        order.setCustomer(customer);
        order.setStatus(from);

        stubManagerAndOrder(managerId, manager, orderId, order);
        stubBuildResponse(orderId, order, customerId);

        UpdateOrderStatusRequestDto request = new UpdateOrderStatusRequestDto(
                to,
                null,
                null,
                null,
                "note"
        );

        orderService.updateStatus(orderId, request);

        assertThat(order.getStatus()).isEqualTo(to);

        ArgumentCaptor<OrderStatusHistoryEntity> historyCaptor = ArgumentCaptor.forClass(OrderStatusHistoryEntity.class);
        verify(orderStatusHistoryRepository).save(historyCaptor.capture());
        OrderStatusHistoryEntity savedHistory = historyCaptor.getValue();
        assertThat(savedHistory.getFromStatus()).isEqualTo(from);
        assertThat(savedHistory.getToStatus()).isEqualTo(to);
        assertThat(savedHistory.getChangedByAccount().getId()).isEqualTo(managerId);

        if (expectReferral) {
            verify(referralService).onOrderDelivered(orderId);
        } else {
            verify(referralService, never()).onOrderDelivered(any());
        }
    }

    @Test
    void updateStatus_withOwnerRole_succeeds() {
        UUID ownerId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        AccountEntity owner = new AccountEntity();
        owner.setId(ownerId);
        owner.setRole(AccountRole.OWNER);

        AccountEntity customer = new AccountEntity();
        customer.setId(customerId);

        OrderEntity order = new OrderEntity();
        order.setId(orderId);
        order.setCustomer(customer);
        order.setStatus(OrderStatus.CREATED);

        stubManagerAndOrder(ownerId, owner, orderId, order);
        stubBuildResponse(orderId, order, customerId);

        orderService.updateStatus(
                orderId,
                new UpdateOrderStatusRequestDto(OrderStatus.PROCESSING, null, null, null, null)
        );

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PROCESSING);
        verify(referralService, never()).onOrderDelivered(any());
    }

    @Test
    void updateStatus_sameStatus_throwsBadRequest() {
        UUID adminId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        AccountEntity admin = new AccountEntity();
        admin.setId(adminId);
        admin.setRole(AccountRole.ADMIN);

        AccountEntity customer = new AccountEntity();
        OrderEntity order = new OrderEntity();
        order.setId(orderId);
        order.setCustomer(customer);
        order.setStatus(OrderStatus.PROCESSING);

        when(securityContextHelper.getCurrentAccountIdOrThrow()).thenReturn(adminId);
        when(accountRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateStatus(
                orderId,
                new UpdateOrderStatusRequestDto(OrderStatus.PROCESSING, null, null, null, null)
        ))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode().value())
                .isEqualTo(BAD_REQUEST.value());
    }

    @Test
    void updateStatus_fromDelivered_throwsBadRequest() {
        UUID adminId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        AccountEntity admin = new AccountEntity();
        admin.setId(adminId);
        admin.setRole(AccountRole.ADMIN);

        OrderEntity order = new OrderEntity();
        order.setId(orderId);
        order.setCustomer(new AccountEntity());
        order.setStatus(OrderStatus.DELIVERED);

        when(securityContextHelper.getCurrentAccountIdOrThrow()).thenReturn(adminId);
        when(accountRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateStatus(
                orderId,
                new UpdateOrderStatusRequestDto(OrderStatus.PROCESSING, null, null, null, null)
        ))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode().value())
                .isEqualTo(BAD_REQUEST.value());
    }

    @Test
    void updateStatus_toDelivered_withDeliveryWindow_throwsBadRequest() {
        UUID adminId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        AccountEntity admin = new AccountEntity();
        admin.setId(adminId);
        admin.setRole(AccountRole.ADMIN);

        OrderEntity order = new OrderEntity();
        order.setId(orderId);
        order.setCustomer(new AccountEntity());
        order.setStatus(OrderStatus.SHIPPED);

        when(securityContextHelper.getCurrentAccountIdOrThrow()).thenReturn(adminId);
        when(accountRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        Instant at = Instant.parse("2026-01-01T00:00:00Z");
        assertThatThrownBy(() -> orderService.updateStatus(
                orderId,
                new UpdateOrderStatusRequestDto(OrderStatus.DELIVERED, at, null, null, null)
        ))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode().value())
                .isEqualTo(BAD_REQUEST.value());
    }

    @Test
    void updateStatus_toProcessing_setsDeliveryWindowOnOrder() {
        UUID adminId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        AccountEntity admin = new AccountEntity();
        admin.setId(adminId);
        admin.setRole(AccountRole.ADMIN);

        AccountEntity customer = new AccountEntity();
        customer.setId(customerId);

        OrderEntity order = new OrderEntity();
        order.setId(orderId);
        order.setCustomer(customer);
        order.setStatus(OrderStatus.CREATED);

        stubManagerAndOrder(adminId, admin, orderId, order);
        stubBuildResponse(orderId, order, customerId);

        Instant start = Instant.parse("2026-03-01T08:00:00Z");
        Instant end = Instant.parse("2026-03-05T18:00:00Z");

        orderService.updateStatus(
                orderId,
                new UpdateOrderStatusRequestDto(OrderStatus.PROCESSING, start, end, null, null)
        );

        assertThat(order.getEstimatedDeliveryAt()).isEqualTo(start);
        assertThat(order.getEstimatedDeliveryEnd()).isEqualTo(end);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PROCESSING);

        ArgumentCaptor<OrderStatusHistoryEntity> historyCaptor = ArgumentCaptor.forClass(OrderStatusHistoryEntity.class);
        verify(orderStatusHistoryRepository).save(historyCaptor.capture());
        assertThat(historyCaptor.getValue().getEstimatedDeliveryAt()).isEqualTo(start);
    }

    @Test
    void getById_customerOtherOrder_throwsForbidden() {
        UUID viewerId = UUID.randomUUID();
        UUID ownerCustomerId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        AccountEntity viewer = new AccountEntity();
        viewer.setId(viewerId);
        viewer.setRole(AccountRole.CUSTOMER);

        AccountEntity orderOwner = new AccountEntity();
        orderOwner.setId(ownerCustomerId);

        OrderEntity order = new OrderEntity();
        order.setId(orderId);
        order.setCustomer(orderOwner);

        when(securityContextHelper.getCurrentAccountIdOrThrow()).thenReturn(viewerId);
        when(accountRepository.findById(viewerId)).thenReturn(Optional.of(viewer));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.getById(orderId))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode().value())
                .isEqualTo(FORBIDDEN.value());
    }

    @Test
    void getById_admin_canReadAnyOrder() {
        UUID adminId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        AccountEntity admin = new AccountEntity();
        admin.setId(adminId);
        admin.setRole(AccountRole.ADMIN);

        AccountEntity customer = new AccountEntity();
        customer.setId(customerId);

        OrderEntity order = new OrderEntity();
        order.setId(orderId);
        order.setCustomer(customer);
        order.setStatus(OrderStatus.CREATED);

        when(securityContextHelper.getCurrentAccountIdOrThrow()).thenReturn(adminId);
        when(accountRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderItemRepository.findAllByOrderId(orderId)).thenReturn(Collections.emptyList());
        when(orderStatusHistoryRepository.findAllByOrderId(orderId)).thenReturn(Collections.emptyList());

        OrderResponseDto dto = new OrderResponseDto(
                orderId,
                1L,
                customerId,
                OrderStatus.CREATED,
                "Создан",
                "a",
                null,
                OrderPaymentMethod.CARD_ONLINE,
                "x",
                null,
                null,
                null,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                null,
                null,
                null,
                null,
                null,
                null,
                Collections.emptyList(),
                Collections.emptyList(),
                null,
                null
        );
        when(orderMapper.toResponse(eq(order), any(), any())).thenReturn(dto);

        assertThat(orderService.getById(orderId)).isEqualTo(dto);
    }

    @Test
    void getById_missingOrder_throwsNotFound() {
        UUID adminId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        AccountEntity admin = new AccountEntity();
        admin.setId(adminId);
        admin.setRole(AccountRole.ADMIN);

        when(securityContextHelper.getCurrentAccountIdOrThrow()).thenReturn(adminId);
        when(accountRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getById(orderId))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode().value())
                .isEqualTo(NOT_FOUND.value());
    }

    @Test
    void search_admin_passesFilterToRepository() {
        UUID adminId = UUID.randomUUID();
        UUID filterCustomerId = UUID.randomUUID();

        AccountEntity admin = new AccountEntity();
        admin.setId(adminId);
        admin.setRole(AccountRole.ADMIN);

        when(securityContextHelper.getCurrentAccountIdOrThrow()).thenReturn(adminId);
        when(accountRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(orderRepository.findAll(org.mockito.ArgumentMatchers.<Specification<OrderEntity>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        orderService.search(new OrderSearchRequestDto(filterCustomerId, OrderStatus.PROCESSING, null), PageRequest.of(0, 20));

        verify(orderRepository, times(1)).findAll(
                org.mockito.ArgumentMatchers.<Specification<OrderEntity>>any(),
                any(Pageable.class)
        );
    }

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
        product.setStockQuantity(10);

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
        when(productRepository.findAllWithTaxonomyByIdsForUpdate(any())).thenReturn(List.of(product));
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
                BigDecimal.ZERO,
                new BigDecimal("180.00"),
                null,
                null,
                null,
                null,
                null,
                null,
                Collections.emptyList(),
                Collections.emptyList(),
                null,
                null
        ));

        CreateOrderRequestDto request = new CreateOrderRequestDto(
                "Street 1",
                null,
                OrderPaymentMethod.CARD_OR_ON_RECEIPT,
                List.of(new CreateOrderItemRequestDto(productId, 2)),
                null,
                null
        );

        OrderResponseDto response = orderService.create(request);

        assertThat(response.totalPrice()).isEqualByComparingTo("200.00");
        assertThat(response.totalDiscountedPrice()).isEqualByComparingTo("180.00");
        verify(orderItemRepository).save(any(OrderItemEntity.class));
        verify(orderStatusHistoryRepository).save(any(OrderStatusHistoryEntity.class));
    }

    @Test
    void create_shouldThrowBadRequest_whenProductInactive() {
        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        AccountEntity customer = new AccountEntity();
        customer.setId(customerId);
        customer.setRole(AccountRole.CUSTOMER);

        ProductEntity product = new ProductEntity();
        product.setId(productId);
        product.setPrice(new BigDecimal("10.00"));
        product.setDiscountPercent(BigDecimal.ZERO);
        product.setIsActive(false);
        product.setStockQuantity(10);

        when(securityContextHelper.getCurrentAccountIdOrThrow()).thenReturn(customerId);
        when(accountRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(productRepository.findAllWithTaxonomyByIdsForUpdate(any())).thenReturn(List.of(product));

        CreateOrderRequestDto request = new CreateOrderRequestDto(
                "Addr",
                null,
                OrderPaymentMethod.CARD_OR_ON_RECEIPT,
                List.of(new CreateOrderItemRequestDto(productId, 1)),
                null,
                null
        );

        assertThatThrownBy(() -> orderService.create(request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> {
                    ResponseStatusException ex = (ResponseStatusException) error;
                    assertThat(ex.getStatusCode().value()).isEqualTo(BAD_REQUEST.value());
                    assertThat(ex.getReason()).contains("inactive");
                    assertThat(ex.getReason()).contains(productId.toString());
                });

        verify(orderItemRepository, never()).save(any(OrderItemEntity.class));
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

        orderService.search(new OrderSearchRequestDto(UUID.randomUUID(), OrderStatus.CREATED, null), PageRequest.of(0, 20));

        verify(orderRepository).findAll(org.mockito.ArgumentMatchers.<Specification<OrderEntity>>any(), any(Pageable.class));
    }

    private void stubManagerAndOrder(UUID managerId, AccountEntity manager, UUID orderId, OrderEntity order) {
        when(securityContextHelper.getCurrentAccountIdOrThrow()).thenReturn(managerId);
        when(accountRepository.findById(managerId)).thenReturn(Optional.of(manager));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private void stubBuildResponse(UUID orderId, OrderEntity order, UUID customerId) {
        when(orderItemRepository.findAllByOrderId(orderId)).thenReturn(Collections.emptyList());
        when(orderStatusHistoryRepository.findAllByOrderId(orderId)).thenReturn(Collections.emptyList());
        when(orderMapper.toResponse(eq(order), any(), any())).thenAnswer(invocation -> {
            OrderEntity o = invocation.getArgument(0);
            return new OrderResponseDto(
                    orderId,
                    1L,
                    customerId,
                    o.getStatus(),
                    "l",
                    "addr",
                    null,
                    OrderPaymentMethod.CARD_ONLINE,
                    "x",
                    null,
                    o.getEstimatedDeliveryAt(),
                    o.getEstimatedDeliveryEnd(),
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    Collections.emptyList(),
                    Collections.emptyList(),
                    null,
                    null
            );
        });
    }
}
