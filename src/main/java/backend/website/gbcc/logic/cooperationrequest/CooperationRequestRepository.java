package backend.website.gbcc.logic.cooperationrequest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface CooperationRequestRepository extends JpaRepository<CooperationRequestEntity, UUID>,
        JpaSpecificationExecutor<CooperationRequestEntity> {
}
