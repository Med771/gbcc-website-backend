package backend.website.gbcc.logic.product;

import backend.website.gbcc.logic.product.productclass.ProductClassEntity;
import backend.website.gbcc.logic.product.productseries.ProductSeriesEntity;
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
@Table(name = "product")
@Getter
@Setter
@NoArgsConstructor
public class ProductEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_id", nullable = false)
    private ProductClassEntity productClass;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "series_id")
    private ProductSeriesEntity productSeries;

    @Column(nullable = false)
    private String brand;

    @Column(columnDefinition = "text")
    private String description;

    @Column(length = 512)
    private String tagline;

    @Column(length = 512)
    private String tags;

    @Column(name = "interest_count", nullable = false)
    private Integer interestCount = 0;

    @Column(name = "delivery_text", columnDefinition = "text")
    private String deliveryText;

    @Column(name = "licenses_text", columnDefinition = "text")
    private String licensesText;

    @Column(name = "height_mm", nullable = false)
    private Integer heightMm;

    @Column(name = "width_mm", nullable = false)
    private Integer widthMm;

    @Column(name = "length_mm", nullable = false)
    private Integer lengthMm;

    @Column(name = "weight_kg", nullable = false, precision = 12, scale = 3)
    private BigDecimal weightKg;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "discount_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal discountPercent = BigDecimal.ZERO;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "popularity_score", nullable = false)
    private Integer popularityScore = 0;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity = 0;
}
