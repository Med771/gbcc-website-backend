package backend.website.gbcc.model.error;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Schema(description = "Единый формат ошибки API (см. GlobalExceptionHandler)")
public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<ApiErrorField> fields
) {
}
