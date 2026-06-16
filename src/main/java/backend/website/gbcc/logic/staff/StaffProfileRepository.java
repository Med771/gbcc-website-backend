package backend.website.gbcc.logic.staff;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StaffProfileRepository extends JpaRepository<StaffProfileEntity, UUID> {

    boolean existsByDisplayId(String displayId);
}
