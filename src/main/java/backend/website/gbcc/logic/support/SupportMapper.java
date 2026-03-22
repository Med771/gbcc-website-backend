package backend.website.gbcc.logic.support;

import backend.website.gbcc.logic.support.dto.SupportConversationDetailResponseDto;
import backend.website.gbcc.logic.support.dto.SupportConversationSummaryResponseDto;
import backend.website.gbcc.logic.support.dto.SupportMessageResponseDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class SupportMapper {

    public SupportConversationSummaryResponseDto toSummary(SupportConversationEntity entity) {
        UUID customerId = entity.getCustomer() != null ? entity.getCustomer().getId() : null;
        return new SupportConversationSummaryResponseDto(
                entity.getId(),
                entity.getSubject(),
                entity.getStatus(),
                entity.getGuestName(),
                entity.getGuestEmail(),
                customerId,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public SupportConversationDetailResponseDto toDetail(
            SupportConversationEntity entity,
            List<SupportMessageEntity> messages
    ) {
        UUID customerId = entity.getCustomer() != null ? entity.getCustomer().getId() : null;
        List<SupportMessageResponseDto> messageDtos = messages.stream()
                .map(this::toMessageResponse)
                .toList();
        return new SupportConversationDetailResponseDto(
                entity.getId(),
                entity.getSubject(),
                entity.getStatus(),
                entity.getGuestName(),
                entity.getGuestEmail(),
                customerId,
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                messageDtos
        );
    }

    public SupportMessageResponseDto toMessageResponse(SupportMessageEntity entity) {
        UUID authorId = entity.getAuthorAccount() != null ? entity.getAuthorAccount().getId() : null;
        return new SupportMessageResponseDto(
                entity.getId(),
                entity.getAuthorType(),
                authorId,
                entity.getBody(),
                entity.getCreatedAt()
        );
    }
}
