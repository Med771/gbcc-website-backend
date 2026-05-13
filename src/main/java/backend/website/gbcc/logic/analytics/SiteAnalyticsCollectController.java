package backend.website.gbcc.logic.analytics;

import backend.website.gbcc.logic.analytics.dto.SiteAnalyticsCollectRequestDto;
import backend.website.gbcc.model.error.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
@Tag(name = "Site analytics", description = "Публичный first-party сбор событий с сайта (пакетами). Без JWT.")
public class SiteAnalyticsCollectController {

    private final SiteAnalyticsCollectService collectService;

    @Operation(summary = "Отправить пакет событий", description = """
            Публичный endpoint. При включённой cookie-политике может выставить HttpOnly cookie посетителя,
            если её нет или значение не совпадает с переданным visitorId. Заголовок DNT: 1 — события не сохраняются
            (если app.analytics.honor-dnt=true). Сырой IP не сохраняется.
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Принято (или отключено / DNT — без записи)"),
            @ApiResponse(responseCode = "400", description = "Валидация или лимиты",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/collect")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void collect(
            @Valid @RequestBody SiteAnalyticsCollectRequestDto body,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        collectService.collect(body, request, response);
    }
}
