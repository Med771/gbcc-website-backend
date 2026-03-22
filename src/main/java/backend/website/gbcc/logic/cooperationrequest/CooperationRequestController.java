package backend.website.gbcc.logic.cooperationrequest;

import backend.website.gbcc.config.OpenApiConstants;
import backend.website.gbcc.logic.cooperationrequest.dto.CooperationRequestResponseDto;
import backend.website.gbcc.logic.cooperationrequest.dto.CreateCooperationRequestDto;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/cooperation-requests")
@RequiredArgsConstructor
@Validated
@Tag(name = "Cooperation requests", description = """
        Заявки с формы «Сотрудничество с заводом»: публичная отправка; список и назначение — только ADMIN/OWNER (JWT).
        Вложение: UUID файла после загрузки через POST /file (multipart). Тип сотрудничества — произвольная строка (как на фронте).
        """)
public class CooperationRequestController {

    private final CooperationRequestService cooperationRequestService;

    @Operation(summary = "Отправить заявку", description = "Публично. Требуется согласие на обработку персональных данных.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Создана",
                    content = @Content(schema = @Schema(implementation = CooperationRequestResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Валидация или файл не найден",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CooperationRequestResponseDto create(@Valid @RequestBody CreateCooperationRequestDto requestDto) {
        return cooperationRequestService.create(requestDto);
    }

    @Operation(
            summary = "Список заявок (администраторы)",
            description = "JWT. Фильтры: onlyUnassigned, query (поиск по имени, email, телефону, комментарию, типу сотрудничества)."
    )
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Страница заявок",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "401", description = "Нет JWT",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Не менеджер",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping
    public PageResponse<CooperationRequestResponseDto> searchForAdmin(
            @RequestParam(required = false) Boolean onlyUnassigned,
            @RequestParam(required = false) String query,
            @Parameter(hidden = true) Pageable pageable
    ) {
        return cooperationRequestService.searchForAdmin(onlyUnassigned, query, pageable);
    }

    @Operation(summary = "Заявка по id (администраторы)", description = "JWT. Полная карточка с назначением и id вложения.")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Найдена",
                    content = @Content(schema = @Schema(implementation = CooperationRequestResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Нет JWT",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Не менеджер",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Не найдена",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public CooperationRequestResponseDto getById(@PathVariable UUID id) {
        return cooperationRequestService.getByIdForAdmin(id);
    }

    @Operation(
            summary = "Взять заявку на изучение",
            description = "JWT. Назначает текущего администратора. Если уже назначен другой — 409 (OWNER может переназначить на себя)."
    )
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Назначено",
                    content = @Content(schema = @Schema(implementation = CooperationRequestResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Нет JWT",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Не менеджер",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Не найдена",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Уже назначена другому",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/{id}/take")
    public CooperationRequestResponseDto take(@PathVariable UUID id) {
        return cooperationRequestService.take(id);
    }

    @Operation(
            summary = "Снять назначение",
            description = "JWT. Снимает привязку администратора; может назначенный админ или OWNER."
    )
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Снято",
                    content = @Content(schema = @Schema(implementation = CooperationRequestResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Не было назначения",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Нет JWT",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Нет прав",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Не найдена",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/{id}/release")
    public CooperationRequestResponseDto release(@PathVariable UUID id) {
        return cooperationRequestService.release(id);
    }
}
