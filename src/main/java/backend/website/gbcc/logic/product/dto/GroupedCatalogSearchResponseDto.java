package backend.website.gbcc.logic.product.dto;

import java.util.List;

/**
 * Сгруппированный поиск для мобильного меню: классы (категории) и товары (оборудование).
 */
public record GroupedCatalogSearchResponseDto(
        long totalCategoryCount,
        long totalProductCount,
        long totalCount,
        List<ProductClassResponseDto> categories,
        List<ProductResponseDto> products
) {
}
