package backend.website.gbcc.logic.analytics;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface SiteAnalyticsEventRepository extends JpaRepository<SiteAnalyticsEventEntity, UUID>,
        JpaSpecificationExecutor<SiteAnalyticsEventEntity> {

    @Query(value = """
            SELECT COUNT(DISTINCT visitor_id) FROM site_analytics_event
            WHERE received_at >= :from AND received_at < :to
            """, nativeQuery = true)
    long countDistinctVisitors(@Param("from") Instant from, @Param("to") Instant to);

    @Query(value = """
            SELECT COUNT(*) FROM site_analytics_event
            WHERE received_at >= :from AND received_at < :to
            """, nativeQuery = true)
    long countEventsInRange(@Param("from") Instant from, @Param("to") Instant to);

    @Query(value = """
            SELECT path AS path, COUNT(*) AS cnt FROM site_analytics_event
            WHERE received_at >= :from AND received_at < :to
            GROUP BY path
            ORDER BY cnt DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<PathCountProjection> findTopPaths(@Param("from") Instant from, @Param("to") Instant to, @Param("limit") int limit);

    @Query(value = """
            SELECT COUNT(DISTINCT session_id) FROM site_analytics_event
            WHERE received_at >= :since
            """, nativeQuery = true)
    long countDistinctSessionsSince(@Param("since") Instant since);

    interface PathCountProjection {
        String getPath();

        Long getCnt();
    }
}
