package backend.website.gbcc.logic.contactrequest;

import backend.website.gbcc.logic.account.AccountEntity;
import backend.website.gbcc.logic.contactrequest.dto.ContactRequestResponseDto;
import org.springframework.stereotype.Component;

@Component
public class ContactRequestMapper {

    public ContactRequestResponseDto toResponse(ContactRequestEntity entity) {
        AccountEntity assigned = entity.getAssignedTo();
        return new ContactRequestResponseDto(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getPhone(),
                entity.getMessage(),
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
