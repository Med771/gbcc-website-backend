package backend.website.gbcc.logic.promotion;

import backend.website.gbcc.config.OpenApiConstants;
import backend.website.gbcc.logic.promotion.dto.CreatePromotionRequestDto;
import backend.website.gbcc.logic.promotion.dto.PatchPromotionRequestDto;
import backend.website.gbcc.logic.promotion.dto.PromotionResponseDto;
import backend.website.gbcc.logic.promotion.dto.PromotionSearchRequestDto;
import backend.website.gbcc.logic.promotion.dto.UpdatePromotionRequestDto;
import backend.website.gbcc.model.PromotionScopeType;
import backend.website.gbcc.model.dto.PageResponse;
import backend.website.gbcc.model.error.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/promotion")
@RequiredArgsConstructor
@Validated
@Tag(name = "Promotions", description = """
        Акции (процент скидки, период действия, охват: ALL / PRODUCT / CLASS / SERIES / PRODUCT_TYPE).
        GET — публично; для не-менеджеров отдаются только активные и попадающие в срок. Изменение — JWT ADMIN/OWNER.
        """)
public class PromotionController {

    private final PromotionService promotionService;

    @Operation(summary = "Создать акцию", description = "JWT. Только ADMIN/OWNER.")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Создана",
                    content = @Content(schema = @Schema(implementation = PromotionResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные scope/даты",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Нет JWT",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Нет прав",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PromotionResponseDto create(@Valid @RequestBody CreatePromotionRequestDto requestDto) {
        return promotionService.create(requestDto);
    }

    @Operation(summary = "Полное обновление акции", description = "JWT. ADMIN/OWNER.")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Обновлено",
                    content = @Content(schema = @Schema(implementation = PromotionResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка данных",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Нет JWT",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Нет прав",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Не найдена",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PutMapping("/{promotionId}")
    public PromotionResponseDto update(
            @PathVariable UUID promotionId,
            @Valid @RequestBody UpdatePromotionRequestDto requestDto
    ) {
        return promotionService.update(promotionId, requestDto);
    }

    @Operation(summary = "Частичное обновление акции", description = "JWT. ADMIN/OWNER.")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Обновлено",
                    content = @Content(schema = @Schema(implementation = PromotionResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка данных",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Нет JWT",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Нет прав",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Не найдена",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PatchMapping("/{promotionId}")
    public PromotionResponseDto patch(
            @PathVariable UUID promotionId,
            @Valid @RequestBody PatchPromotionRequestDto requestDto
    ) {
        return promotionService.patch(promotionId, requestDto);
    }

    @Operation(
            summary = "Получить акцию по id",
            description = "Публично. Не-менеджеры не получают неактивные или вне окна дат (404)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Найдена",
                    content = @Content(schema = @Schema(implementation = PromotionResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Нет доступа или не найдена",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{promotionId}")
    public PromotionResponseDto getById(@PathVariable UUID promotionId) {
        return promotionService.getById(promotionId);
    }

    @Operation(summary = "Поиск акций", description = "Публично. Фильтры: name, isActive, scope. Для гостей — только видимые акции.")
    @ApiResponse(responseCode = "200", description = "Страница акций",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @GetMapping
    public PageResponse<PromotionResponseDto> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) PromotionScopeType scope,
            @Parameter(hidden = true) Pageable pageable
    ) {
        PromotionSearchRequestDto requestDto = new PromotionSearchRequestDto(name, isActive, scope);
        return promotionService.search(requestDto, pageable);
    }

    @Operation(summary = "Удалить акцию", description = "JWT. ADMIN/OWNER.")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Удалена"),
            @ApiResponse(responseCode = "401", description = "Нет JWT",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Нет прав",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Не найдена",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @DeleteMapping("/{promotionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID promotionId) {
        promotionService.delete(promotionId);
    }
}
