package backend.website.gbcc.logic.crm.map;

import backend.website.gbcc.logic.crm.map.dto.CrmCompanyObjectResponseDto;
import backend.website.gbcc.logic.crm.map.dto.CrmMapPinResponseDto;
import backend.website.gbcc.logic.crm.map.dto.CreateCrmCompanyObjectRequestDto;
import backend.website.gbcc.logic.crm.map.dto.UpdateCrmCompanyObjectRequestDto;

import java.util.List;
import java.util.UUID;

public interface CrmMapService {

    List<CrmMapPinResponseDto> pins();

    CrmCompanyObjectResponseDto createCompanyObject(CreateCrmCompanyObjectRequestDto dto);

    CrmCompanyObjectResponseDto updateCompanyObject(UUID id, UpdateCrmCompanyObjectRequestDto dto);

    void deleteCompanyObject(UUID id);

    List<CrmCompanyObjectResponseDto> listCompanyObjects();
}
