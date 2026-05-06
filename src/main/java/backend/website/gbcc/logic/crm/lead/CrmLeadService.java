package backend.website.gbcc.logic.crm.lead;

import backend.website.gbcc.logic.crm.lead.dto.CreateCrmLeadRequestDto;
import backend.website.gbcc.logic.crm.lead.dto.CrmLeadResponseDto;
import backend.website.gbcc.logic.crm.lead.dto.CrmReassignLeadRequestDto;
import backend.website.gbcc.logic.crm.lead.dto.UpdateCrmLeadRequestDto;
import backend.website.gbcc.logic.crm.organization.dto.CrmOrganizationResponseDto;
import backend.website.gbcc.model.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CrmLeadService {

    CrmLeadResponseDto create(CreateCrmLeadRequestDto dto);

    CrmLeadResponseDto update(UUID id, UpdateCrmLeadRequestDto dto);

    CrmLeadResponseDto reassign(UUID id, CrmReassignLeadRequestDto dto);

    CrmLeadResponseDto getById(UUID id);

    PageResponse<CrmLeadResponseDto> search(String query, Pageable pageable);

    CrmOrganizationResponseDto convertToOrganization(UUID leadId);
}
