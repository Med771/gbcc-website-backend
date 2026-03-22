package backend.website.gbcc.logic.order;

import backend.website.gbcc.logic.product.ProductEntity;
import backend.website.gbcc.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "order_item")
@Getter
@Setter
@NoArgsConstructor
public class OrderItemEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "unit_discount_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal unitDiscountPercent;

    @Column(name = "unit_discounted_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitDiscountedPrice;

    @Column(name = "line_total_price", nullable = false, precision = 14, scale = 2)
    private BigDecimal lineTotalPrice;

    @Column(name = "line_total_discounted_price", nullable = false, precision = 14, scale = 2)
    private BigDecimal lineTotalDiscountedPrice;
}
