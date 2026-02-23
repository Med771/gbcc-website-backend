package backend.website.gbcc.logic.product.dto;

import java.util.UUID;

public record ProductClassResponseDto(
        UUID id,
        String name
) {
}
