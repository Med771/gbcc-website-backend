package backend.website.gbcc.logic.crm.interaction;

import backend.website.gbcc.logic.crm.interaction.dto.CreateCrmInteractionRequestDto;
import backend.website.gbcc.logic.crm.interaction.dto.CrmInteractionResponseDto;
import backend.website.gbcc.logic.crm.interaction.dto.UpdateCrmInteractionRequestDto;
import backend.website.gbcc.model.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.UUID;

public interface CrmInteractionService {

    CrmInteractionResponseDto create(CreateCrmInteractionRequestDto dto);

    CrmInteractionResponseDto update(UUID id, UpdateCrmInteractionRequestDto dto);

    void delete(UUID id);

    CrmInteractionResponseDto getById(UUID id);

    PageResponse<CrmInteractionResponseDto> search(UUID organizationId, UUID leadId, UUID authorId,
                                                    Instant occurredFrom, Instant occurredTo, String resultFragment,
                                                    CrmInteractionType interactionType, Pageable pageable);
}
