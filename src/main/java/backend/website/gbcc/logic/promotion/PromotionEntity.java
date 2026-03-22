package backend.website.gbcc.logic.promotion;

import backend.website.gbcc.model.BaseEntity;
import backend.website.gbcc.model.PromotionScopeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "promotion")
@Getter
@Setter
@NoArgsConstructor
public class PromotionEntity extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(name = "discount_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal discountPercent;

    @Column(name = "valid_from")
    private Instant validFrom;

    @Column(name = "valid_to")
    private Instant validTo;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false)
    private Integer priority = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PromotionScopeType scope;

    @Column(name = "scope_reference_id")
    private UUID scopeReferenceId;
}
