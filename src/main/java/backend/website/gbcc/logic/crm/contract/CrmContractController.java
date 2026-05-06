package backend.website.gbcc.logic.crm.contract;

import backend.website.gbcc.config.OpenApiConstants;
import backend.website.gbcc.logic.crm.contract.dto.CreateCrmContractLineRequestDto;
import backend.website.gbcc.logic.crm.contract.dto.CreateCrmContractRequestDto;
import backend.website.gbcc.logic.crm.contract.dto.CrmContractLineResponseDto;
import backend.website.gbcc.logic.crm.contract.dto.CrmContractResponseDto;
import backend.website.gbcc.logic.crm.contract.dto.UpdateCrmContractLineRequestDto;
import backend.website.gbcc.logic.crm.contract.dto.UpdateCrmContractRequestDto;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/crm/contracts")
@RequiredArgsConstructor
@Validated
@Tag(name = "CRM — Contracts")
public class CrmContractController {

    private final CrmContractService contractService;

    @Operation(summary = "Создать контракт")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CrmContractResponseDto create(@Valid @RequestBody CreateCrmContractRequestDto dto) {
        return contractService.create(dto);
    }

    @Operation(summary = "Обновить контракт")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @PutMapping("/{id}")
    public CrmContractResponseDto update(@PathVariable UUID id, @Valid @RequestBody UpdateCrmContractRequestDto dto) {
        return contractService.update(id, dto);
    }

    @Operation(summary = "Удалить контракт")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        contractService.delete(id);
    }

    @Operation(summary = "Контракт по id")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @GetMapping("/{id}")
    public CrmContractResponseDto getById(@PathVariable UUID id) {
        return contractService.getById(id);
    }

    @Operation(summary = "Поиск контрактов")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @GetMapping
    public PageResponse<CrmContractResponseDto> search(
            @RequestParam(required = false) UUID organizationId,
            @Parameter(hidden = true) Pageable pageable
    ) {
        return contractService.search(organizationId, pageable);
    }

    @Operation(summary = "Строки графика контракта")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @GetMapping("/{contractId}/lines")
    public List<CrmContractLineResponseDto> listLines(@PathVariable UUID contractId) {
        return contractService.listLines(contractId);
    }

    @Operation(summary = "Добавить строку графика")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @PostMapping("/{contractId}/lines")
    @ResponseStatus(HttpStatus.CREATED)
    public CrmContractLineResponseDto addLine(
            @PathVariable UUID contractId,
            @Valid @RequestBody CreateCrmContractLineRequestDto dto
    ) {
        return contractService.addLine(contractId, dto);
    }

    @Operation(summary = "Обновить строку")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @PutMapping("/{contractId}/lines/{lineId}")
    public CrmContractLineResponseDto updateLine(
            @PathVariable UUID contractId,
            @PathVariable UUID lineId,
            @Valid @RequestBody UpdateCrmContractLineRequestDto dto
    ) {
        return contractService.updateLine(contractId, lineId, dto);
    }

    @Operation(summary = "Удалить строку")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @DeleteMapping("/{contractId}/lines/{lineId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLine(@PathVariable UUID contractId, @PathVariable UUID lineId) {
        contractService.deleteLine(contractId, lineId);
    }
}
