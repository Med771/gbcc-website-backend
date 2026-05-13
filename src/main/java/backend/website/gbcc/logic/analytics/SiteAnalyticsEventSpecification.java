package backend.website.gbcc.logic.analytics;

import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

public final class SiteAnalyticsEventSpecification {

    private SiteAnalyticsEventSpecification() {
    }

    /**
     * Фильтр по времени приёма на сервере: {@code [from, to)}.
     */
    public static Specification<SiteAnalyticsEventEntity> receivedBetween(Instant fromInclusive, Instant toExclusive) {
        return (root, q, cb) -> cb.and(
                cb.greaterThanOrEqualTo(root.get("receivedAt"), fromInclusive),
                cb.lessThan(root.get("receivedAt"), toExclusive)
        );
    }
}
