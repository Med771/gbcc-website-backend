package backend.website.gbcc.logic.order;

import backend.website.gbcc.logic.account.AccountEntity;
import backend.website.gbcc.model.BaseEntity;
import backend.website.gbcc.model.OrderPaymentMethod;
import backend.website.gbcc.model.OrderStatus;
import backend.website.gbcc.model.convector.OrderPaymentMethodConverter;
import backend.website.gbcc.model.convector.OrderStatusConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
public class OrderEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private AccountEntity customer;

    @Convert(converter = OrderStatusConverter.class)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.CREATED;

    @Column(name = "delivery_address", nullable = false, columnDefinition = "text")
    private String deliveryAddress;

    @Column(name = "customer_comment", columnDefinition = "text")
    private String customerComment;

    @Column(name = "estimated_delivery_at")
    private Instant estimatedDeliveryAt;

    @Column(name = "estimated_delivery_end")
    private Instant estimatedDeliveryEnd;

    @Column(name = "display_number", nullable = false, unique = true)
    private Long displayNumber;

    @Convert(converter = OrderPaymentMethodConverter.class)
    @Column(name = "payment_method", nullable = false, length = 32)
    private OrderPaymentMethod paymentMethod = OrderPaymentMethod.CARD_OR_ON_RECEIPT;

    @Column(name = "receipt_url", columnDefinition = "text")
    private String receiptUrl;

    @Column(name = "total_price", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "total_discounted_price", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalDiscountedPrice;
}
