package backend.website.gbcc.logic.support;

import backend.website.gbcc.logic.support.dto.AddSupportMessageRequestDto;
import backend.website.gbcc.logic.support.dto.CreateSupportConversationRequestDto;
import backend.website.gbcc.logic.support.dto.CreateSupportConversationResponseDto;
import backend.website.gbcc.logic.support.dto.SupportConversationDetailResponseDto;
import backend.website.gbcc.logic.support.dto.SupportConversationSummaryResponseDto;
import backend.website.gbcc.logic.support.dto.SupportSearchRequestDto;
import backend.website.gbcc.logic.support.dto.UpdateSupportConversationStatusRequestDto;
import backend.website.gbcc.model.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface SupportService {

    CreateSupportConversationResponseDto createConversation(CreateSupportConversationRequestDto requestDto);

    SupportConversationDetailResponseDto getConversation(UUID conversationId, String supportToken);

    PageResponse<SupportConversationSummaryResponseDto> searchForAdmin(SupportSearchRequestDto filter, Pageable pageable);

    PageResponse<SupportConversationSummaryResponseDto> listMine(Pageable pageable);

    SupportConversationDetailResponseDto addMessage(
            UUID conversationId,
            AddSupportMessageRequestDto requestDto,
            String supportToken
    );

    SupportConversationDetailResponseDto updateStatus(
            UUID conversationId,
            UpdateSupportConversationStatusRequestDto requestDto
    );
}
