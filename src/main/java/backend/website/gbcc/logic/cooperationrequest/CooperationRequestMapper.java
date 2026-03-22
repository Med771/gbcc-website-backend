package backend.website.gbcc.logic.cooperationrequest;

import backend.website.gbcc.logic.account.AccountEntity;
import backend.website.gbcc.logic.cooperationrequest.dto.CooperationRequestResponseDto;
import org.springframework.stereotype.Component;

@Component
public class CooperationRequestMapper {

    public CooperationRequestResponseDto toResponse(CooperationRequestEntity entity) {
        AccountEntity assigned = entity.getAssignedTo();
        return new CooperationRequestResponseDto(
                entity.getId(),
                entity.getName(),
                entity.getPhone(),
                entity.getEmail(),
                entity.getCooperationType(),
                entity.getComment(),
                entity.getAttachmentFile() != null ? entity.getAttachmentFile().getId() : null,
                entity.getConsentProcessing(),
                assigned != null ? assigned.getId() : null,
                assigned != null ? assigned.getName() : null,
                assigned != null ? assigned.getEmail() : null,
                entity.getAssignedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
