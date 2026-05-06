package backend.website.gbcc.logic.crm.task;

import backend.website.gbcc.logic.crm.task.dto.CreateCrmTaskRequestDto;
import backend.website.gbcc.logic.crm.task.dto.CrmTaskResponseDto;
import backend.website.gbcc.logic.crm.task.dto.UpdateCrmTaskRequestDto;
import backend.website.gbcc.model.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface CrmTaskService {

    CrmTaskResponseDto create(CreateCrmTaskRequestDto dto);

    CrmTaskResponseDto update(UUID id, UpdateCrmTaskRequestDto dto);

    void delete(UUID id);

    CrmTaskResponseDto getById(UUID id);

    PageResponse<CrmTaskResponseDto> search(UUID organizationId, UUID leadId, CrmTaskStatus status,
                                            UUID assigneeId, Instant dueAfter, Instant dueBefore, Pageable pageable);

    List<CrmTaskResponseDto> overdue();
}
