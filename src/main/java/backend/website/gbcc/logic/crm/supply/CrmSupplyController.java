package backend.website.gbcc.logic.crm.supply;

import backend.website.gbcc.config.OpenApiConstants;
import backend.website.gbcc.logic.crm.supply.dto.CreateCrmSupplyRequestDto;
import backend.website.gbcc.logic.crm.supply.dto.CrmSupplyResponseDto;
import backend.website.gbcc.logic.crm.supply.dto.UpdateCrmSupplyRequestDto;
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

import java.util.UUID;

@RestController
@RequestMapping("/crm/supplies")
@RequiredArgsConstructor
@Validated
@Tag(name = "CRM — Supplies")
public class CrmSupplyController {

    private final CrmSupplyService supplyService;

    @Operation(summary = "Создать поставку")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CrmSupplyResponseDto create(@Valid @RequestBody CreateCrmSupplyRequestDto dto) {
        return supplyService.create(dto);
    }

    @Operation(summary = "Обновить")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @PutMapping("/{id}")
    public CrmSupplyResponseDto update(@PathVariable UUID id, @Valid @RequestBody UpdateCrmSupplyRequestDto dto) {
        return supplyService.update(id, dto);
    }

    @Operation(summary = "Удалить")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        supplyService.delete(id);
    }

    @Operation(summary = "По id")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @GetMapping("/{id}")
    public CrmSupplyResponseDto getById(@PathVariable UUID id) {
        return supplyService.getById(id);
    }

    @Operation(summary = "Поиск")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @GetMapping
    public PageResponse<CrmSupplyResponseDto> search(
            @RequestParam(required = false) UUID organizationId,
            @Parameter(hidden = true) Pageable pageable
    ) {
        return supplyService.search(organizationId, pageable);
    }
}
