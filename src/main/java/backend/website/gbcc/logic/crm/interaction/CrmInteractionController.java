package backend.website.gbcc.logic.crm.interaction;

import backend.website.gbcc.config.OpenApiConstants;
import backend.website.gbcc.logic.crm.interaction.dto.CreateCrmInteractionRequestDto;
import backend.website.gbcc.logic.crm.interaction.dto.CrmInteractionResponseDto;
import backend.website.gbcc.logic.crm.interaction.dto.UpdateCrmInteractionRequestDto;
import backend.website.gbcc.model.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/crm/interactions")
@RequiredArgsConstructor
@Validated
@Tag(name = "CRM — Interactions")
public class CrmInteractionController {

    private final CrmInteractionService interactionService;

    @Operation(summary = "Создать запись взаимодействия")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CrmInteractionResponseDto create(@Valid @RequestBody CreateCrmInteractionRequestDto dto) {
        return interactionService.create(dto);
    }

    @Operation(summary = "Обновить (автор или OWNER)")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @PutMapping("/{id}")
    public CrmInteractionResponseDto update(@PathVariable UUID id, @Valid @RequestBody UpdateCrmInteractionRequestDto dto) {
        return interactionService.update(id, dto);
    }

    @Operation(summary = "Удалить (автор или OWNER)")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        interactionService.delete(id);
    }

    @Operation(summary = "Получить по id")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @GetMapping("/{id}")
    public CrmInteractionResponseDto getById(@PathVariable UUID id) {
        return interactionService.getById(id);
    }

    @Operation(summary = "Поиск с фильтрами")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @GetMapping
    public PageResponse<CrmInteractionResponseDto> search(
            @RequestParam(required = false) UUID organizationId,
            @RequestParam(required = false) UUID leadId,
            @RequestParam(required = false) UUID authorId,
            @RequestParam(required = false) Instant occurredFrom,
            @RequestParam(required = false) Instant occurredTo,
            @RequestParam(required = false) String resultFragment,
            @Parameter(hidden = true) Pageable pageable
    ) {
        return interactionService.search(organizationId, leadId, authorId, occurredFrom, occurredTo, resultFragment, pageable);
    }
}
