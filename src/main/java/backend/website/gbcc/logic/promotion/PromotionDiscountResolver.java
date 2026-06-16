package backend.website.gbcc.logic.promotion;

import backend.website.gbcc.logic.product.ProductEntity;
import backend.website.gbcc.model.PromotionScopeType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

/**
 * Combines per-product {@code discountPercent} with the best matching promotion at {@code at}.
 * Effective discount = min(100%, max(productDiscount, bestPromotionDiscount)).
 */
@Component
@RequiredArgsConstructor
public class PromotionDiscountResolver {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private final PromotionRepository promotionRepository;

    public BigDecimal resolveEffectiveDiscountPercent(ProductEntity product, BigDecimal productDiscountPercent, Instant at) {
        BigDecimal base = clampPercent(productDiscountPercent);
        List<PromotionEntity> active = promotionRepository.findAllActiveAt(at);
        BigDecimal bestPromo = active.stream()
                .filter(p -> matchesScope(p, product))
                .max(Comparator
                        .comparing(PromotionEntity::getDiscountPercent)
                        .thenComparing(PromotionEntity::getPriority))
                .map(PromotionEntity::getDiscountPercent)
                .map(this::clampPercent)
                .orElse(BigDecimal.ZERO);

        BigDecimal effective = base.max(bestPromo);
        if (effective.compareTo(ONE_HUNDRED) > 0) {
            return ONE_HUNDRED;
        }
        return effective.setScale(2, RoundingMode.HALF_UP);
    }

    private boolean matchesScope(PromotionEntity promotion, ProductEntity product) {
        return switch (promotion.getScope()) {
            case ALL -> true;
            case PRODUCT -> promotion.getScopeReferenceId().equals(product.getId());
            case CLASS -> product.getProductClass() != null
                    && promotion.getScopeReferenceId().equals(product.getProductClass().getId());
            case SERIES -> product.getProductSeries() != null
                    && promotion.getScopeReferenceId().equals(product.getProductSeries().getId());
        };
    }

    private BigDecimal clampPercent(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }
        if (value.compareTo(ONE_HUNDRED) > 0) {
            return ONE_HUNDRED;
        }
        return value;
    }
}
