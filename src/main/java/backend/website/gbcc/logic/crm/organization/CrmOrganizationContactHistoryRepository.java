package backend.website.gbcc.logic.crm.organization;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CrmOrganizationContactHistoryRepository extends JpaRepository<CrmOrganizationContactHistoryEntity, UUID> {

    List<CrmOrganizationContactHistoryEntity> findByContact_IdOrderByCreatedAtDesc(UUID contactId);
}
