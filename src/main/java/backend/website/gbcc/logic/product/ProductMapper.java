package backend.website.gbcc.logic.product;

import backend.website.gbcc.logic.product.dto.CreateProductRequestDto;
import backend.website.gbcc.logic.product.dto.ProductResponseDto;
import backend.website.gbcc.logic.product.productclass.ProductClassEntity;
import backend.website.gbcc.logic.product.productseries.ProductSeriesEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "productClass", source = "productClass")
    @Mapping(target = "productSeries", source = "productSeries")
    @Mapping(target = "brand", source = "request.brand")
    @Mapping(target = "description", source = "request.description")
    @Mapping(target = "heightMm", source = "request.heightMm")
    @Mapping(target = "widthMm", source = "request.widthMm")
    @Mapping(target = "lengthMm", source = "request.lengthMm")
    @Mapping(target = "weightKg", source = "request.weightKg")
    @Mapping(target = "price", source = "request.price")
    @Mapping(target = "discountPercent", expression = "java(resolveDiscountPercent(request.discountPercent()))")
    @Mapping(target = "isActive", expression = "java(resolveIsActive(request))")
    @Mapping(target = "popularityScore", constant = "0")
    @Mapping(target = "interestCount", constant = "0")
    @Mapping(target = "stockQuantity", expression = "java(resolveStockQuantity(request.stockQuantity()))")
    @Mapping(target = "tagline", source = "request.tagline")
    @Mapping(target = "tags", source = "request.tags")
    @Mapping(target = "deliveryText", source = "request.deliveryText")
    @Mapping(target = "licensesText", source = "request.licensesText")
    ProductEntity toEntity(
            CreateProductRequestDto request,
            ProductClassEntity productClass,
            ProductSeriesEntity productSeries
    );

    @Mapping(target = "id", source = "entity.id")
    @Mapping(target = "className", expression = "java(entity.getProductClass().getName())")
    @Mapping(target = "seriesName", expression = "java(entity.getProductSeries() != null ? entity.getProductSeries().getName() : null)")
    @Mapping(target = "tagline", source = "entity.tagline")
    @Mapping(target = "tags", source = "entity.tags")
    @Mapping(target = "interestCount", source = "entity.interestCount")
    @Mapping(target = "deliveryText", source = "entity.deliveryText")
    @Mapping(target = "licensesText", source = "entity.licensesText")
    @Mapping(target = "popularityScore", source = "entity.popularityScore")
    @Mapping(target = "originalPrice", source = "entity.price")
    @Mapping(target = "finalPrice", expression = "java(calculateDiscountedPrice(entity.getPrice(), entity.getDiscountPercent()))")
    @Mapping(target = "priceLabel", expression = "java(resolvePriceLabel(entity.getPrice()))")
    @Mapping(target = "priceLabelHint", expression = "java(resolvePriceLabelHint(entity.getPrice()))")
    @Mapping(target = "discountPercent", source = "entity.discountPercent")
    @Mapping(target = "discountedPrice", expression = "java(calculateDiscountedPrice(entity.getPrice(), entity.getDiscountPercent()))")
    @Mapping(target = "stockQuantity", source = "entity.stockQuantity")
    @Mapping(target = "photoFileIds", source = "photoFileIds")
    @Mapping(target = "createdAt", source = "entity.createdAt")
    @Mapping(target = "updatedAt", source = "entity.updatedAt")
    ProductResponseDto toResponse(ProductEntity entity, List<UUID> photoFileIds);

    default Boolean resolveIsActive(CreateProductRequestDto request) {
        return request.isActive() != null ? request.isActive() : Boolean.TRUE;
    }

    default Integer resolveStockQuantity(Integer stockQuantity) {
        return stockQuantity != null ? stockQuantity : 0;
    }

    default BigDecimal resolveDiscountPercent(BigDecimal discountPercent) {
        return discountPercent != null ? discountPercent : BigDecimal.ZERO;
    }

    default BigDecimal calculateDiscountedPrice(BigDecimal price, BigDecimal discountPercent) {
        if (price == null) {
            return null;
        }
        BigDecimal safeDiscount = discountPercent != null ? discountPercent : BigDecimal.ZERO;
        BigDecimal multiplier = BigDecimal.ONE.subtract(safeDiscount.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
        return price.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
    }

    default String resolvePriceLabel(BigDecimal price) {
        if (price != null && price.compareTo(BigDecimal.ZERO) == 0) {
            return "Договорная";
        }
        return null;
    }

    default String resolvePriceLabelHint(BigDecimal price) {
        if (price != null && price.compareTo(BigDecimal.ZERO) == 0) {
            return "Цена по запросу";
        }
        return null;
    }

    default String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
