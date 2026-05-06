package backend.website.gbcc.logic.crm.contract;

import backend.website.gbcc.logic.crm.contract.dto.CreateCrmContractLineRequestDto;
import backend.website.gbcc.logic.crm.contract.dto.CreateCrmContractRequestDto;
import backend.website.gbcc.logic.crm.contract.dto.CrmContractLineResponseDto;
import backend.website.gbcc.logic.crm.contract.dto.CrmContractResponseDto;
import backend.website.gbcc.logic.crm.contract.dto.UpdateCrmContractLineRequestDto;
import backend.website.gbcc.logic.crm.contract.dto.UpdateCrmContractRequestDto;
import backend.website.gbcc.model.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface CrmContractService {

    CrmContractResponseDto create(CreateCrmContractRequestDto dto);

    CrmContractResponseDto update(UUID id, UpdateCrmContractRequestDto dto);

    void delete(UUID id);

    CrmContractResponseDto getById(UUID id);

    PageResponse<CrmContractResponseDto> search(UUID organizationId, Pageable pageable);

    CrmContractLineResponseDto addLine(UUID contractId, CreateCrmContractLineRequestDto dto);

    CrmContractLineResponseDto updateLine(UUID contractId, UUID lineId, UpdateCrmContractLineRequestDto dto);

    void deleteLine(UUID contractId, UUID lineId);

    List<CrmContractLineResponseDto> listLines(UUID contractId);
}
