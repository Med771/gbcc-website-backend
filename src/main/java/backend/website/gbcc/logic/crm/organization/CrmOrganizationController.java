package backend.website.gbcc.logic.crm.organization;

import backend.website.gbcc.config.OpenApiConstants;
import backend.website.gbcc.logic.crm.organization.dto.CreateCrmOrganizationContactRequestDto;
import backend.website.gbcc.logic.crm.organization.dto.CreateCrmOrganizationRequestDto;
import backend.website.gbcc.logic.crm.organization.dto.CrmOrganizationContactHistoryResponseDto;
import backend.website.gbcc.logic.crm.organization.dto.CrmOrganizationContactResponseDto;
import backend.website.gbcc.logic.crm.organization.dto.CrmOrganizationResponseDto;
import backend.website.gbcc.logic.crm.organization.dto.CrmReassignOrganizationRequestDto;
import backend.website.gbcc.logic.crm.organization.dto.UpdateCrmOrganizationContactRequestDto;
import backend.website.gbcc.logic.crm.organization.dto.UpdateCrmOrganizationRequestDto;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/crm/organizations")
@RequiredArgsConstructor
@Validated
@Tag(name = "CRM — Organizations")
public class CrmOrganizationController {

    private final CrmOrganizationService organizationService;

    @Operation(summary = "Создать организацию")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CrmOrganizationResponseDto create(@Valid @RequestBody CreateCrmOrganizationRequestDto dto) {
        return organizationService.create(dto);
    }

    @Operation(summary = "Обновить организацию")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @PutMapping("/{id}")
    public CrmOrganizationResponseDto update(@PathVariable UUID id, @Valid @RequestBody UpdateCrmOrganizationRequestDto dto) {
        return organizationService.update(id, dto);
    }

    @Operation(summary = "Переназначить ответственного (только OWNER)")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @PatchMapping("/{id}/assign")
    public CrmOrganizationResponseDto reassign(@PathVariable UUID id, @Valid @RequestBody CrmReassignOrganizationRequestDto dto) {
        return organizationService.reassign(id, dto);
    }

    @Operation(summary = "Карточка организации")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @GetMapping("/{id}")
    public CrmOrganizationResponseDto getById(@PathVariable UUID id) {
        return organizationService.getById(id);
    }

    @Operation(summary = "Поиск организаций (ADMIN — только свои; OWNER — все)")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @GetMapping
    public PageResponse<CrmOrganizationResponseDto> search(
            @RequestParam(required = false) String query,
            @Parameter(hidden = true) Pageable pageable
    ) {
        return organizationService.search(query, pageable);
    }

    @Operation(summary = "Список контактов организации")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @GetMapping("/{organizationId}/contacts")
    public List<CrmOrganizationContactResponseDto> listContacts(@PathVariable UUID organizationId) {
        return organizationService.listContacts(organizationId);
    }

    @Operation(summary = "Добавить контакт")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @PostMapping("/{organizationId}/contacts")
    @ResponseStatus(HttpStatus.CREATED)
    public CrmOrganizationContactResponseDto addContact(
            @PathVariable UUID organizationId,
            @Valid @RequestBody CreateCrmOrganizationContactRequestDto dto
    ) {
        return organizationService.addContact(organizationId, dto);
    }

    @Operation(summary = "Обновить контакт (пишет историю изменений)")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @PutMapping("/{organizationId}/contacts/{contactId}")
    public CrmOrganizationContactResponseDto updateContact(
            @PathVariable UUID organizationId,
            @PathVariable UUID contactId,
            @Valid @RequestBody UpdateCrmOrganizationContactRequestDto dto
    ) {
        return organizationService.updateContact(organizationId, contactId, dto);
    }

    @Operation(summary = "Удалить контакт")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @DeleteMapping("/{organizationId}/contacts/{contactId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteContact(@PathVariable UUID organizationId, @PathVariable UUID contactId) {
        organizationService.deleteContact(organizationId, contactId);
    }

    @Operation(summary = "История изменений контакта")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @GetMapping("/{organizationId}/contacts/{contactId}/history")
    public List<CrmOrganizationContactHistoryResponseDto> contactHistory(
            @PathVariable UUID organizationId,
            @PathVariable UUID contactId
    ) {
        return organizationService.listContactHistory(organizationId, contactId);
    }
}
