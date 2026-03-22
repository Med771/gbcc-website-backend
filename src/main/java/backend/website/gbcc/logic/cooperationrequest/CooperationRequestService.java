package backend.website.gbcc.logic.cooperationrequest;

import backend.website.gbcc.logic.cooperationrequest.dto.CooperationRequestResponseDto;
import backend.website.gbcc.logic.cooperationrequest.dto.CreateCooperationRequestDto;
import backend.website.gbcc.model.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CooperationRequestService {

    CooperationRequestResponseDto create(CreateCooperationRequestDto requestDto);

    PageResponse<CooperationRequestResponseDto> searchForAdmin(Boolean onlyUnassigned, String query, Pageable pageable);

    CooperationRequestResponseDto getByIdForAdmin(UUID id);

    CooperationRequestResponseDto take(UUID id);

    CooperationRequestResponseDto release(UUID id);
}
