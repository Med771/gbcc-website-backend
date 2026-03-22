package backend.website.gbcc.logic.promotion.dto;

import backend.website.gbcc.model.PromotionScopeType;

public record PromotionSearchRequestDto(
        String name,
        Boolean isActive,
        PromotionScopeType scope
) {
}
