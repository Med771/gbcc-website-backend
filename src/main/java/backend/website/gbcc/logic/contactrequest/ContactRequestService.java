package backend.website.gbcc.logic.contactrequest;

import backend.website.gbcc.logic.contactrequest.dto.ContactRequestResponseDto;
import backend.website.gbcc.logic.contactrequest.dto.CreateContactRequestDto;
import backend.website.gbcc.model.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ContactRequestService {

    ContactRequestResponseDto create(CreateContactRequestDto requestDto);

    PageResponse<ContactRequestResponseDto> searchForAdmin(Boolean onlyUnassigned, String query, Pageable pageable);

    ContactRequestResponseDto getByIdForAdmin(UUID id);

    ContactRequestResponseDto take(UUID id);

    ContactRequestResponseDto release(UUID id);
}
