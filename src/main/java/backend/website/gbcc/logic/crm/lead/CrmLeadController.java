package backend.website.gbcc.logic.crm.lead;

import backend.website.gbcc.config.OpenApiConstants;
import backend.website.gbcc.logic.crm.lead.dto.CreateCrmLeadRequestDto;
import backend.website.gbcc.logic.crm.lead.dto.CrmLeadResponseDto;
import backend.website.gbcc.logic.crm.lead.dto.CrmReassignLeadRequestDto;
import backend.website.gbcc.logic.crm.lead.dto.UpdateCrmLeadRequestDto;
import backend.website.gbcc.logic.crm.organization.dto.CrmOrganizationResponseDto;
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
@RequestMapping("/crm/leads")
@RequiredArgsConstructor
@Validated
@Tag(name = "CRM — Leads")
public class CrmLeadController {

    private final CrmLeadService leadService;

    @Operation(summary = "Создать лид")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CrmLeadResponseDto create(@Valid @RequestBody CreateCrmLeadRequestDto dto) {
        return leadService.create(dto);
    }

    @Operation(summary = "Обновить лид")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @PutMapping("/{id}")
    public CrmLeadResponseDto update(@PathVariable UUID id, @Valid @RequestBody UpdateCrmLeadRequestDto dto) {
        return leadService.update(id, dto);
    }

    @Operation(summary = "Переназначить ответственного (OWNER)")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @PatchMapping("/{id}/assign")
    public CrmLeadResponseDto reassign(@PathVariable UUID id, @Valid @RequestBody CrmReassignLeadRequestDto dto) {
        return leadService.reassign(id, dto);
    }

    @Operation(summary = "Конвертировать в организацию")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @PostMapping("/{id}/convert")
    @ResponseStatus(HttpStatus.CREATED)
    public CrmOrganizationResponseDto convert(@PathVariable UUID id) {
        return leadService.convertToOrganization(id);
    }

    @Operation(summary = "По id")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @GetMapping("/{id}")
    public CrmLeadResponseDto getById(@PathVariable UUID id) {
        return leadService.getById(id);
    }

    @Operation(summary = "Поиск")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @GetMapping
    public PageResponse<CrmLeadResponseDto> search(
            @RequestParam(required = false) String query,
            @Parameter(hidden = true) Pageable pageable
    ) {
        return leadService.search(query, pageable);
    }
}
