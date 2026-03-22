package backend.website.gbcc.logic.news;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface NewsRepository extends JpaRepository<NewsEntity, UUID>, JpaSpecificationExecutor<NewsEntity> {
}
