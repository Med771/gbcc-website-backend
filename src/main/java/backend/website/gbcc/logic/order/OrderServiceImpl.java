package backend.website.gbcc.logic.order;

import backend.website.gbcc.helper.SecurityContextHelper;
import backend.website.gbcc.logic.account.AccountEntity;
import backend.website.gbcc.logic.account.AccountRepository;
import backend.website.gbcc.logic.crm.access.CrmAccessPolicy;
import backend.website.gbcc.logic.crm.organization.CrmOrganizationEntity;
import backend.website.gbcc.logic.crm.organization.CrmOrganizationRepository;
import backend.website.gbcc.logic.managercommission.ManagerCommissionService;
import backend.website.gbcc.logic.order.dto.CreateOrderItemRequestDto;
import backend.website.gbcc.logic.order.dto.CreateOrderRequestDto;
import backend.website.gbcc.logic.order.dto.OrderResponseDto;
import backend.website.gbcc.logic.order.dto.OrderSearchRequestDto;
import backend.website.gbcc.logic.order.dto.PatchOrderManagerRequestDto;
import backend.website.gbcc.logic.order.dto.UpdateOrderStatusRequestDto;
import backend.website.gbcc.logic.product.ProductEntity;
import backend.website.gbcc.logic.product.ProductRepository;
import backend.website.gbcc.logic.promotion.PromotionDiscountResolver;
import backend.website.gbcc.logic.referral.ReferralService;
import backend.website.gbcc.model.AccountRole;
import backend.website.gbcc.model.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final ProductRepository productRepository;
    private final AccountRepository accountRepository;
    private final CrmOrganizationRepository crmOrganizationRepository;
    private final SecurityContextHelper securityContextHelper;
    private final OrderMapper orderMapper;
    private final PromotionDiscountResolver promotionDiscountResolver;
    private final ReferralService referralService;
    private final ManagerCommissionService managerCommissionService;
    private final CrmAccessPolicy crmAccessPolicy;

    @Override
    @Transactional
    public OrderResponseDto create(CreateOrderRequestDto requestDto) {
        AccountEntity requester = getCurrentAccountOrThrow();
        if (requester.getRole() != AccountRole.CUSTOMER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only customer can create order");
        }
        String deliveryAddress = normalizeRequired(requestDto.deliveryAddress(), "deliveryAddress");
        String customerComment = normalizeOptional(requestDto.customerComment());

        Map<UUID, Integer> quantityByProductId = aggregateAndValidateItems(requestDto.items());
        List<ProductEntity> products = productRepository.findAllWithTaxonomyByIdsForUpdate(quantityByProductId.keySet());
        if (products.size() != quantityByProductId.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Some products not found");
        }

        Map<UUID, ProductEntity> productById = products.stream()
                .collect(Collectors.toMap(ProductEntity::getId, p -> p));

        for (Map.Entry<UUID, Integer> entry : quantityByProductId.entrySet()) {
            ProductEntity product = productById.get(entry.getKey());
            if (!Boolean.TRUE.equals(product.getIsActive())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product is inactive: " + product.getId());
            }
            int quantity = entry.getValue();
            if (product.getStockQuantity() < quantity) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient stock for product: " + product.getId());
            }
        }

        OrderEntity order = new OrderEntity();
        order.setDisplayNumber(orderRepository.nextDisplayNumber());
        order.setPaymentMethod(requestDto.paymentMethod());
        order.setCustomer(requester);
        order.setStatus(OrderStatus.CREATED);
        order.setDeliveryAddress(deliveryAddress);
        order.setCustomerComment(customerComment);
        order.setEstimatedDeliveryAt(null);
        order.setEstimatedDeliveryEnd(null);
        order.setDeliveryFee(requestDto.deliveryFee() != null
                ? requestDto.deliveryFee().setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);

        if (requestDto.crmOrganizationId() != null) {
            CrmOrganizationEntity org = crmOrganizationRepository.findById(requestDto.crmOrganizationId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "CRM organization not found"));
            order.setCrmOrganization(org);
            if (requester.getCrmOrganization() == null) {
                requester.setCrmOrganization(org);
                accountRepository.save(requester);
            }
        }

        BigDecimal totalPrice = BigDecimal.ZERO;
        BigDecimal totalDiscountedPrice = BigDecimal.ZERO;
        order.setTotalPrice(totalPrice.setScale(2, RoundingMode.HALF_UP));
        order.setTotalDiscountedPrice(totalDiscountedPrice.setScale(2, RoundingMode.HALF_UP));
        OrderEntity savedOrder = orderRepository.save(order);

        Instant pricingAt = Instant.now();
        for (Map.Entry<UUID, Integer> entry : quantityByProductId.entrySet()) {
            ProductEntity product = productById.get(entry.getKey());
            int quantity = entry.getValue();
            product.setStockQuantity(product.getStockQuantity() - quantity);
            productRepository.save(product);

            BigDecimal unitPrice = product.getPrice();
            BigDecimal unitDiscountPercent = promotionDiscountResolver.resolveEffectiveDiscountPercent(
                    product,
                    product.getDiscountPercent(),
                    pricingAt
            );
            unitDiscountPercent = safeDiscountPercent(unitDiscountPercent);
            BigDecimal unitDiscountedPrice = discounted(unitPrice, unitDiscountPercent);
            BigDecimal quantityDecimal = BigDecimal.valueOf(quantity);
            BigDecimal lineTotalPrice = unitPrice.multiply(quantityDecimal).setScale(2, RoundingMode.HALF_UP);
            BigDecimal lineTotalDiscountedPrice = unitDiscountedPrice.multiply(quantityDecimal).setScale(2, RoundingMode.HALF_UP);

            OrderItemEntity item = new OrderItemEntity();
            item.setOrder(savedOrder);
            item.setProduct(product);
            item.setQuantity(quantity);
            item.setUnitPrice(unitPrice);
            item.setUnitDiscountPercent(unitDiscountPercent);
            item.setUnitDiscountedPrice(unitDiscountedPrice);
            item.setLineTotalPrice(lineTotalPrice);
            item.setLineTotalDiscountedPrice(lineTotalDiscountedPrice);
            orderItemRepository.save(item);

            totalPrice = totalPrice.add(lineTotalPrice);
            totalDiscountedPrice = totalDiscountedPrice.add(lineTotalDiscountedPrice);
        }

        savedOrder.setTotalPrice(totalPrice.setScale(2, RoundingMode.HALF_UP));
        savedOrder.setTotalDiscountedPrice(totalDiscountedPrice.setScale(2, RoundingMode.HALF_UP));
        orderRepository.save(savedOrder);

        addHistory(savedOrder, null, OrderStatus.CREATED, requester, "Order created", null);
        return buildResponse(savedOrder.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDto getById(UUID orderId) {
        AccountEntity requester = getCurrentAccountOrThrow();
        OrderEntity order = findOrderOrThrow(orderId);
        validateReadAccess(requester, order);
        return buildResponse(orderId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponseDto> search(OrderSearchRequestDto requestDto, Pageable pageable) {
        AccountEntity requester = getCurrentAccountOrThrow();
        OrderSearchRequestDto safeRequest = requestDto != null
                ? requestDto
                : new OrderSearchRequestDto(null, null, null);

        OrderSearchRequestDto effectiveRequest = safeRequest;
        if (requester.getRole() == AccountRole.CUSTOMER) {
            effectiveRequest = new OrderSearchRequestDto(requester.getId(), safeRequest.status(), null);
        }

        return orderRepository.findAll(OrderSpecification.byFilter(effectiveRequest), pageable)
                .map(order -> buildResponse(order.getId()));
    }

    @Override
    @Transactional
    public OrderResponseDto updateStatus(UUID orderId, UpdateOrderStatusRequestDto requestDto) {
        AccountEntity requester = getCurrentAccountOrThrow();
        ensureManagerRole(requester);

        OrderEntity order = findOrderOrThrow(orderId);
        OrderStatus fromStatus = order.getStatus();
        OrderStatus toStatus = requestDto.status();
        validateStatusTransition(fromStatus, toStatus);

        String comment = normalizeOptional(requestDto.comment());
        Instant estimatedDeliveryAt = requestDto.estimatedDeliveryAt();
        Instant estimatedDeliveryEnd = requestDto.estimatedDeliveryEnd();

        if (toStatus == OrderStatus.DELIVERED || toStatus == OrderStatus.CANCELLED) {
            if (estimatedDeliveryAt != null || estimatedDeliveryEnd != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Final order status cannot have delivery window");
            }
            order.setEstimatedDeliveryAt(null);
            order.setEstimatedDeliveryEnd(null);
        } else {
            if (estimatedDeliveryAt != null) {
                order.setEstimatedDeliveryAt(estimatedDeliveryAt);
            }
            if (estimatedDeliveryEnd != null) {
                order.setEstimatedDeliveryEnd(estimatedDeliveryEnd);
            }
        }

        if (requestDto.receiptUrl() != null) {
            order.setReceiptUrl(normalizeOptional(requestDto.receiptUrl()));
        }

        if (toStatus == OrderStatus.CANCELLED && fromStatus != OrderStatus.DELIVERED) {
            restoreStockForOrder(orderId);
        }

        order.setStatus(toStatus);
        orderRepository.save(order);

        addHistory(order, fromStatus, toStatus, requester, comment, order.getEstimatedDeliveryAt());
        if (toStatus == OrderStatus.DELIVERED) {
            referralService.onOrderDelivered(orderId);
            managerCommissionService.onOrderDelivered(orderId);
        }
        return buildResponse(orderId);
    }

    @Override
    @Transactional
    public OrderResponseDto patchByManager(UUID orderId, PatchOrderManagerRequestDto requestDto) {
        AccountEntity requester = getCurrentAccountOrThrow();
        ensureManagerRole(requester);

        OrderEntity order = findOrderOrThrow(orderId);
        if (requestDto.contactName() != null) {
            order.setContactName(normalizeOptional(requestDto.contactName()));
        }
        if (requestDto.contactPhone() != null) {
            order.setContactPhone(normalizeOptional(requestDto.contactPhone()));
        }
        if (requestDto.contactEmail() != null) {
            order.setContactEmail(normalizeOptional(requestDto.contactEmail()));
        }
        if (requestDto.managerNotes() != null) {
            order.setManagerNotes(normalizeOptional(requestDto.managerNotes()));
        }
        if (requestDto.deliveryFee() != null) {
            if (requestDto.deliveryFee().compareTo(BigDecimal.ZERO) < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "deliveryFee must be greater or equal to 0");
            }
            order.setDeliveryFee(requestDto.deliveryFee().setScale(2, RoundingMode.HALF_UP));
        }
        if (requestDto.crmOrganizationId() != null) {
            CrmOrganizationEntity org = crmOrganizationRepository.findById(requestDto.crmOrganizationId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "CRM organization not found"));
            if (requester.getRole() == AccountRole.ADMIN) {
                crmAccessPolicy.assertCanModifyOrganization(
                        org.getAssignedTo() != null ? org.getAssignedTo().getId() : null
                );
            }
            order.setCrmOrganization(org);
        }

        orderRepository.save(order);
        return buildResponse(orderId);
    }

    private void restoreStockForOrder(UUID orderId) {
        List<OrderItemEntity> items = orderItemRepository.findAllByOrderId(orderId);
        for (OrderItemEntity item : items) {
            ProductEntity product = productRepository.findById(item.getProduct().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Product not found for stock restore"));
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            productRepository.save(product);
        }
    }

    private void validateReadAccess(AccountEntity requester, OrderEntity order) {
        if (requester.getRole() == AccountRole.CUSTOMER
                && !Objects.equals(order.getCustomer().getId(), requester.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Customer can access only own orders");
        }
    }

    private void ensureManagerRole(AccountEntity requester) {
        if (requester.getRole() == AccountRole.ADMIN || requester.getRole() == AccountRole.OWNER) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admin or owner can update order status");
    }

    private void validateStatusTransition(OrderStatus fromStatus, OrderStatus toStatus) {
        if (fromStatus == toStatus) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order already has this status");
        }
        if (fromStatus == OrderStatus.CANCELLED || fromStatus == OrderStatus.DELIVERED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Final order status cannot be changed");
        }

        boolean valid = switch (fromStatus) {
            case CREATED -> toStatus == OrderStatus.PROCESSING || toStatus == OrderStatus.CANCELLED;
            case PROCESSING -> toStatus == OrderStatus.PACKED || toStatus == OrderStatus.CANCELLED;
            case PACKED -> toStatus == OrderStatus.SHIPPED || toStatus == OrderStatus.CANCELLED;
            case SHIPPED -> toStatus == OrderStatus.DELIVERED || toStatus == OrderStatus.CANCELLED;
            default -> throw new IllegalStateException("Unexpected value: " + fromStatus);
        };

        if (!valid) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid order status transition");
        }
    }

    private void addHistory(
            OrderEntity order,
            OrderStatus fromStatus,
            OrderStatus toStatus,
            AccountEntity changedBy,
            String comment,
            Instant estimatedDeliveryAt
    ) {
        OrderStatusHistoryEntity history = new OrderStatusHistoryEntity();
        history.setOrder(order);
        history.setFromStatus(fromStatus);
        history.setToStatus(toStatus);
        history.setChangedByAccount(changedBy);
        history.setComment(comment);
        history.setEstimatedDeliveryAt(estimatedDeliveryAt);
        orderStatusHistoryRepository.save(history);
    }

    private Map<UUID, Integer> aggregateAndValidateItems(List<CreateOrderItemRequestDto> items) {
        if (items == null || items.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "items must not be empty");
        }

        Map<UUID, Integer> quantityByProductId = new HashMap<>();
        for (CreateOrderItemRequestDto item : items) {
            if (item.productId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "productId is required");
            }
            if (item.quantity() == null || item.quantity() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quantity must be greater than 0");
            }
            quantityByProductId.merge(item.productId(), item.quantity(), Integer::sum);
        }
        return quantityByProductId;
    }

    private OrderEntity findOrderOrThrow(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
    }

    private AccountEntity getCurrentAccountOrThrow() {
        UUID accountId = securityContextHelper.getCurrentAccountIdOrThrow();
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account not found"));
    }

    private String normalizeRequired(String value, String field) {
        if (!StringUtils.hasText(value)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, field + " is required");
        }
        return value.trim();
    }

    private String normalizeOptional(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private BigDecimal safeDiscountPercent(BigDecimal discountPercent) {
        if (discountPercent == null) {
            return BigDecimal.ZERO;
        }
        if (discountPercent.compareTo(BigDecimal.ZERO) < 0 || discountPercent.compareTo(ONE_HUNDRED) > 0) {
            return BigDecimal.ZERO;
        }
        return discountPercent;
    }

    private BigDecimal discounted(BigDecimal price, BigDecimal discountPercent) {
        BigDecimal multiplier = BigDecimal.ONE.subtract(discountPercent.divide(ONE_HUNDRED, 4, RoundingMode.HALF_UP));
        return price.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
    }

    private OrderResponseDto buildResponse(UUID orderId) {
        OrderEntity order = findOrderOrThrow(orderId);
        List<OrderItemEntity> items = orderItemRepository.findAllByOrderId(orderId);
        List<OrderStatusHistoryEntity> history = orderStatusHistoryRepository.findAllByOrderId(orderId);
        return orderMapper.toResponse(order, items, history);
    }
}
