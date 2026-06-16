package backend.website.gbcc.logic.crm.organization;

import backend.website.gbcc.logic.crm.organization.dto.CreateCrmOrganizationBranchRequestDto;
import backend.website.gbcc.logic.crm.organization.dto.CreateCrmOrganizationContactRequestDto;
import backend.website.gbcc.logic.crm.organization.dto.CreateCrmOrganizationRequestDto;
import backend.website.gbcc.logic.crm.organization.dto.CrmOrganizationBranchResponseDto;
import backend.website.gbcc.logic.crm.organization.dto.CrmOrganizationContactHistoryResponseDto;
import backend.website.gbcc.logic.crm.organization.dto.CrmOrganizationContactResponseDto;
import backend.website.gbcc.logic.crm.organization.dto.CrmOrganizationResponseDto;
import backend.website.gbcc.logic.crm.organization.dto.CrmReassignOrganizationRequestDto;
import backend.website.gbcc.logic.crm.organization.dto.UpdateCrmOrganizationBranchRequestDto;
import backend.website.gbcc.logic.crm.organization.dto.UpdateCrmOrganizationContactRequestDto;
import backend.website.gbcc.logic.crm.organization.dto.UpdateCrmOrganizationRequestDto;
import backend.website.gbcc.model.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface CrmOrganizationService {

    CrmOrganizationResponseDto create(CreateCrmOrganizationRequestDto dto);

    CrmOrganizationResponseDto update(UUID id, UpdateCrmOrganizationRequestDto dto);

    CrmOrganizationResponseDto reassign(UUID id, CrmReassignOrganizationRequestDto dto);

    CrmOrganizationResponseDto getById(UUID id);

    PageResponse<CrmOrganizationResponseDto> search(String query, Pageable pageable);

    CrmOrganizationContactResponseDto addContact(UUID organizationId, CreateCrmOrganizationContactRequestDto dto);

    CrmOrganizationContactResponseDto updateContact(UUID organizationId, UUID contactId, UpdateCrmOrganizationContactRequestDto dto);

    void deleteContact(UUID organizationId, UUID contactId);

    List<CrmOrganizationContactResponseDto> listContacts(UUID organizationId);

    List<CrmOrganizationContactHistoryResponseDto> listContactHistory(UUID organizationId, UUID contactId);

    List<CrmOrganizationBranchResponseDto> listBranches(UUID organizationId);

    CrmOrganizationBranchResponseDto createBranch(UUID organizationId, CreateCrmOrganizationBranchRequestDto dto);

    CrmOrganizationBranchResponseDto updateBranch(UUID organizationId, UUID branchId, UpdateCrmOrganizationBranchRequestDto dto);

    void deleteBranch(UUID organizationId, UUID branchId);
}
