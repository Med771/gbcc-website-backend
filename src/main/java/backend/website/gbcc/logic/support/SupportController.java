package backend.website.gbcc.logic.support;

import backend.website.gbcc.config.OpenApiConstants;
import backend.website.gbcc.logic.support.dto.AddSupportMessageRequestDto;
import backend.website.gbcc.logic.support.dto.CreateSupportConversationRequestDto;
import backend.website.gbcc.logic.support.dto.CreateSupportConversationResponseDto;
import backend.website.gbcc.logic.support.dto.SupportConversationDetailResponseDto;
import backend.website.gbcc.logic.support.dto.SupportConversationSummaryResponseDto;
import backend.website.gbcc.logic.support.dto.SupportSearchRequestDto;
import backend.website.gbcc.logic.support.dto.UpdateSupportConversationStatusRequestDto;
import backend.website.gbcc.model.SupportConversationStatus;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/support/conversations")
@RequiredArgsConstructor
@Validated
@Tag(name = "Support", description = """
        Обращения в администрацию. Гость: имя + email + первое сообщение; в ответе guestAccessToken — сохранить и передавать в заголовке X-Support-Token.
        Клиент с JWT создаёт обращение без гостевых полей. Все ADMIN/OWNER видят список и переписку.
        """)
public class SupportController {

    private final SupportService supportService;

    @Operation(
            summary = "Создать обращение",
            description = """
                    Без JWT: обязательны message, guestName, guestEmail (валидный email).
                    С JWT и ролью CUSTOMER: только message (и опционально subject); гостевой токен не выдаётся.
                    Администраторам создавать диалог через этот метод нельзя — отвечают в существующем.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Создано; для гостя в теле guestAccessToken",
                    content = @Content(schema = @Schema(implementation = CreateSupportConversationResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные данные",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Попытка создать от имени администратора",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateSupportConversationResponseDto create(@Valid @RequestBody CreateSupportConversationRequestDto requestDto) {
        return supportService.createConversation(requestDto);
    }

    @Operation(summary = "Мои обращения (клиент)", description = "JWT. Только роль CUSTOMER. Пагинация Spring.")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Страница обращений",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "401", description = "Нет JWT",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Не клиент",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/my")
    public PageResponse<SupportConversationSummaryResponseDto> listMine(@Parameter(hidden = true) Pageable pageable) {
        return supportService.listMine(pageable);
    }

    @Operation(
            summary = "Список обращений (администраторы)",
            description = "Требует JWT. В сервисе допускаются только ADMIN и OWNER. Фильтры: status, query (тема или email гостя)."
    )
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Страница",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "401", description = "Нет JWT",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Не менеджер",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping
    public PageResponse<SupportConversationSummaryResponseDto> searchForAdmin(
            @RequestParam(required = false) SupportConversationStatus status,
            @RequestParam(required = false) String query,
            @Parameter(hidden = true) Pageable pageable
    ) {
        SupportSearchRequestDto filter = new SupportSearchRequestDto(status, query);
        return supportService.searchForAdmin(filter, pageable);
    }

    @Operation(
            summary = "Получить переписку по id",
            description = """
                    Доступ: JWT ADMIN/OWNER (любой диалог); JWT CUSTOMER (свой диалог); гость — заголовок X-Support-Token (схема supportGuestToken в Authorize).
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Диалог с сообщениями",
                    content = @Content(schema = @Schema(implementation = SupportConversationDetailResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Нет доступа",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Не найдено",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{conversationId}")
    public SupportConversationDetailResponseDto get(
            @PathVariable UUID conversationId,
            @Parameter(description = "Токен гостя (если обращение без аккаунта)")
            @RequestHeader(value = OpenApiConstants.SUPPORT_TOKEN_HEADER_NAME, required = false) String supportToken
    ) {
        return supportService.getConversation(conversationId, supportToken);
    }

    @Operation(
            summary = "Добавить сообщение",
            description = """
                    Приоритет: если валиден X-Support-Token для этого диалога — сообщение от гостя (даже при наличии JWT).
                    Иначе: ADMIN/OWNER — ответ администратора; CUSTOMER — только владелец диалога.
                    Закрытый диалог — ошибка 400.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Обновлённый диалог",
                    content = @Content(schema = @Schema(implementation = SupportConversationDetailResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Диалог закрыт или неверные данные",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Нет прав",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Диалог не найден",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/{conversationId}/messages")
    public SupportConversationDetailResponseDto addMessage(
            @PathVariable UUID conversationId,
            @Valid @RequestBody AddSupportMessageRequestDto requestDto,
            @RequestHeader(value = OpenApiConstants.SUPPORT_TOKEN_HEADER_NAME, required = false) String supportToken
    ) {
        return supportService.addMessage(conversationId, requestDto, supportToken);
    }

    @Operation(summary = "Изменить статус обращения", description = "JWT. Только ADMIN/OWNER (например CLOSED).")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Обновлено",
                    content = @Content(schema = @Schema(implementation = SupportConversationDetailResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Нет JWT",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Не менеджер",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Не найдено",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PatchMapping("/{conversationId}/status")
    public SupportConversationDetailResponseDto updateStatus(
            @PathVariable UUID conversationId,
            @Valid @RequestBody UpdateSupportConversationStatusRequestDto requestDto
    ) {
        return supportService.updateStatus(conversationId, requestDto);
    }
}
