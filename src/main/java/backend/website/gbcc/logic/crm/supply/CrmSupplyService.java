package backend.website.gbcc.logic.crm.supply;

import backend.website.gbcc.logic.crm.supply.dto.CreateCrmSupplyRequestDto;
import backend.website.gbcc.logic.crm.supply.dto.CrmSupplyResponseDto;
import backend.website.gbcc.logic.crm.supply.dto.UpdateCrmSupplyRequestDto;
import backend.website.gbcc.model.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CrmSupplyService {

    CrmSupplyResponseDto create(CreateCrmSupplyRequestDto dto);

    CrmSupplyResponseDto update(UUID id, UpdateCrmSupplyRequestDto dto);

    void delete(UUID id);

    CrmSupplyResponseDto getById(UUID id);

    PageResponse<CrmSupplyResponseDto> search(UUID organizationId, Pageable pageable);
}
