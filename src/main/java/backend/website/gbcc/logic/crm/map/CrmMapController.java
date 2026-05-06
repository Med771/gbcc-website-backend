package backend.website.gbcc.logic.crm.map;

import backend.website.gbcc.config.OpenApiConstants;
import backend.website.gbcc.logic.crm.map.dto.CrmCompanyObjectResponseDto;
import backend.website.gbcc.logic.crm.map.dto.CrmMapPinResponseDto;
import backend.website.gbcc.logic.crm.map.dto.CreateCrmCompanyObjectRequestDto;
import backend.website.gbcc.logic.crm.map.dto.UpdateCrmCompanyObjectRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/crm/map")
@RequiredArgsConstructor
@Validated
@Tag(name = "CRM — Map")
public class CrmMapController {

    private final CrmMapService mapService;

    @Operation(summary = "Точки для карты (организации с координатами, поставки с адресом доставки, объекты компании)")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @GetMapping("/pins")
    public List<CrmMapPinResponseDto> pins() {
        return mapService.pins();
    }

    @Operation(summary = "Список объектов компании")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @GetMapping("/company-objects")
    public List<CrmCompanyObjectResponseDto> listObjects() {
        return mapService.listCompanyObjects();
    }

    @Operation(summary = "Создать объект компании")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @PostMapping("/company-objects")
    @ResponseStatus(HttpStatus.CREATED)
    public CrmCompanyObjectResponseDto createObject(@Valid @RequestBody CreateCrmCompanyObjectRequestDto dto) {
        return mapService.createCompanyObject(dto);
    }

    @Operation(summary = "Обновить объект компании")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @PutMapping("/company-objects/{id}")
    public CrmCompanyObjectResponseDto updateObject(@PathVariable UUID id, @Valid @RequestBody UpdateCrmCompanyObjectRequestDto dto) {
        return mapService.updateCompanyObject(id, dto);
    }

    @Operation(summary = "Удалить объект компании")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @DeleteMapping("/company-objects/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteObject(@PathVariable UUID id) {
        mapService.deleteCompanyObject(id);
    }
}
