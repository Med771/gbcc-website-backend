package backend.website.gbcc.logic.contactrequest;

import backend.website.gbcc.config.OpenApiConstants;
import backend.website.gbcc.logic.contactrequest.dto.ContactRequestResponseDto;
import backend.website.gbcc.logic.contactrequest.dto.CreateContactRequestDto;
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
@RequestMapping("/contact-requests")
@RequiredArgsConstructor
@Validated
@Tag(name = "Contact requests", description = """
        Заявки с формы «Свяжитесь с нами»: публичная отправка; список и назначение — только ADMIN/OWNER (JWT).
        Администратор может взять заявку на изучение (привязка к себе) или снять назначение.
        """)
public class ContactRequestController {

    private final ContactRequestService contactRequestService;

    @Operation(summary = "Отправить заявку", description = "Публично. Требуется согласие на обработку персональных данных.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Создана",
                    content = @Content(schema = @Schema(implementation = ContactRequestResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Валидация",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContactRequestResponseDto create(@Valid @RequestBody CreateContactRequestDto requestDto) {
        return contactRequestService.create(requestDto);
    }

    @Operation(
            summary = "Список заявок (администраторы)",
            description = "JWT. Фильтры: onlyUnassigned (только без назначенного), query (поиск по имени, email, телефону, тексту сообщения)."
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
    public PageResponse<ContactRequestResponseDto> searchForAdmin(
            @RequestParam(required = false) Boolean onlyUnassigned,
            @RequestParam(required = false) String query,
            @Parameter(hidden = true) Pageable pageable
    ) {
        return contactRequestService.searchForAdmin(onlyUnassigned, query, pageable);
    }

    @Operation(summary = "Заявка по id (администраторы)", description = "JWT. Полная карточка с назначением.")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Найдена",
                    content = @Content(schema = @Schema(implementation = ContactRequestResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Нет JWT",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Не менеджер",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Не найдена",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ContactRequestResponseDto getById(@PathVariable UUID id) {
        return contactRequestService.getByIdForAdmin(id);
    }

    @Operation(
            summary = "Взять заявку на изучение",
            description = "JWT. Назначает текущего администратора. Если уже назначен другой — 409 (OWNER может переназначить на себя)."
    )
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Назначено",
                    content = @Content(schema = @Schema(implementation = ContactRequestResponseDto.class))),
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
    public ContactRequestResponseDto take(@PathVariable UUID id) {
        return contactRequestService.take(id);
    }

    @Operation(
            summary = "Снять назначение",
            description = "JWT. Снимает привязку администратора; может назначенный админ или OWNER."
    )
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Снято",
                    content = @Content(schema = @Schema(implementation = ContactRequestResponseDto.class))),
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
    public ContactRequestResponseDto release(@PathVariable UUID id) {
        return contactRequestService.release(id);
    }
}
