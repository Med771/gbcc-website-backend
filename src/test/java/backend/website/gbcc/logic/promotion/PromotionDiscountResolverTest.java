package backend.website.gbcc.logic.promotion;

import backend.website.gbcc.logic.product.ProductEntity;
import backend.website.gbcc.logic.product.productclass.ProductClassEntity;
import backend.website.gbcc.logic.product.productseries.ProductSeriesEntity;
import backend.website.gbcc.logic.product.producttype.ProductTypeEntity;
import backend.website.gbcc.model.PromotionScopeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PromotionDiscountResolverTest {

    @Mock
    private PromotionRepository promotionRepository;

    @InjectMocks
    private PromotionDiscountResolver resolver;

    private Instant at;
    private UUID productId;
    private ProductEntity product;
    private ProductClassEntity productClass;

    @BeforeEach
    void setUp() {
        at = Instant.parse("2025-06-01T12:00:00Z");
        productId = UUID.randomUUID();
        productClass = new ProductClassEntity();
        productClass.setId(UUID.randomUUID());

        product = new ProductEntity();
        product.setId(productId);
        product.setProductClass(productClass);
        product.setDiscountPercent(new BigDecimal("10.00"));
    }

    @Test
    void returnsBaseWhenNoPromotions() {
        when(promotionRepository.findAllActiveAt(at)).thenReturn(List.of());

        BigDecimal result = resolver.resolveEffectiveDiscountPercent(product, product.getDiscountPercent(), at);

        assertThat(result).isEqualByComparingTo("10.00");
    }

    @Test
    void takesMaxOfBaseAndAllScopePromotion() {
        PromotionEntity promo = new PromotionEntity();
        promo.setScope(PromotionScopeType.ALL);
        promo.setDiscountPercent(new BigDecimal("25.00"));
        promo.setPriority(0);

        when(promotionRepository.findAllActiveAt(at)).thenReturn(List.of(promo));

        BigDecimal result = resolver.resolveEffectiveDiscountPercent(product, product.getDiscountPercent(), at);

        assertThat(result).isEqualByComparingTo("25.00");
    }

    @Test
    void ignoresProductScopeWhenIdDoesNotMatch() {
        PromotionEntity promo = new PromotionEntity();
        promo.setScope(PromotionScopeType.PRODUCT);
        promo.setScopeReferenceId(UUID.randomUUID());
        promo.setDiscountPercent(new BigDecimal("40.00"));

        when(promotionRepository.findAllActiveAt(at)).thenReturn(List.of(promo));

        BigDecimal result = resolver.resolveEffectiveDiscountPercent(product, product.getDiscountPercent(), at);

        assertThat(result).isEqualByComparingTo("10.00");
    }

    @Test
    void appliesProductScopeWhenIdMatches() {
        PromotionEntity promo = new PromotionEntity();
        promo.setScope(PromotionScopeType.PRODUCT);
        promo.setScopeReferenceId(productId);
        promo.setDiscountPercent(new BigDecimal("30.00"));

        when(promotionRepository.findAllActiveAt(at)).thenReturn(List.of(promo));

        BigDecimal result = resolver.resolveEffectiveDiscountPercent(product, product.getDiscountPercent(), at);

        assertThat(result).isEqualByComparingTo("30.00");
    }

    @Test
    void classScopeMatchesProductClass() {
        PromotionEntity promo = new PromotionEntity();
        promo.setScope(PromotionScopeType.CLASS);
        promo.setScopeReferenceId(productClass.getId());
        promo.setDiscountPercent(new BigDecimal("15.00"));

        when(promotionRepository.findAllActiveAt(at)).thenReturn(List.of(promo));

        BigDecimal result = resolver.resolveEffectiveDiscountPercent(product, product.getDiscountPercent(), at);

        assertThat(result).isEqualByComparingTo("15.00");
    }

    @Test
    void seriesScopeDoesNotMatchWhenSeriesNull() {
        PromotionEntity promo = new PromotionEntity();
        promo.setScope(PromotionScopeType.SERIES);
        promo.setScopeReferenceId(UUID.randomUUID());
        promo.setDiscountPercent(new BigDecimal("50.00"));

        when(promotionRepository.findAllActiveAt(at)).thenReturn(List.of(promo));

        BigDecimal result = resolver.resolveEffectiveDiscountPercent(product, product.getDiscountPercent(), at);

        assertThat(result).isEqualByComparingTo("10.00");
    }

    @Test
    void seriesScopeMatchesWhenSeriesSet() {
        ProductSeriesEntity series = new ProductSeriesEntity();
        series.setId(UUID.randomUUID());
        product.setProductSeries(series);

        PromotionEntity promo = new PromotionEntity();
        promo.setScope(PromotionScopeType.SERIES);
        promo.setScopeReferenceId(series.getId());
        promo.setDiscountPercent(new BigDecimal("18.00"));

        when(promotionRepository.findAllActiveAt(at)).thenReturn(List.of(promo));

        BigDecimal result = resolver.resolveEffectiveDiscountPercent(product, product.getDiscountPercent(), at);

        assertThat(result).isEqualByComparingTo("18.00");
    }

    @Test
    void productTypeScopeMatches() {
        ProductTypeEntity type = new ProductTypeEntity();
        type.setId(UUID.randomUUID());
        product.setProductType(type);

        PromotionEntity promo = new PromotionEntity();
        promo.setScope(PromotionScopeType.PRODUCT_TYPE);
        promo.setScopeReferenceId(type.getId());
        promo.setDiscountPercent(new BigDecimal("12.00"));

        when(promotionRepository.findAllActiveAt(at)).thenReturn(List.of(promo));

        BigDecimal result = resolver.resolveEffectiveDiscountPercent(product, product.getDiscountPercent(), at);

        assertThat(result).isEqualByComparingTo("12.00");
    }

    @Test
    void picksBestPromotionWhenMultipleMatch() {
        PromotionEntity low = new PromotionEntity();
        low.setScope(PromotionScopeType.ALL);
        low.setDiscountPercent(new BigDecimal("12.00"));
        low.setPriority(0);

        PromotionEntity high = new PromotionEntity();
        high.setScope(PromotionScopeType.ALL);
        high.setDiscountPercent(new BigDecimal("20.00"));
        high.setPriority(0);

        when(promotionRepository.findAllActiveAt(at)).thenReturn(List.of(low, high));

        BigDecimal result = resolver.resolveEffectiveDiscountPercent(product, product.getDiscountPercent(), at);

        assertThat(result).isEqualByComparingTo("20.00");
    }
}
