package backend.website.gbcc.logic.product.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.UUID;

public record AttachProductPhotoRequestDto(
        @NotNull(message = "fileId is required")
        UUID fileId,
        @PositiveOrZero(message = "sortOrder must be greater or equal to 0")
        Integer sortOrder
) {
}
