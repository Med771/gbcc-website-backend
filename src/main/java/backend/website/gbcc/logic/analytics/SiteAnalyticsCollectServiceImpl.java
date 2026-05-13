package backend.website.gbcc.logic.analytics;

import backend.website.gbcc.config.property.AnalyticsProperty;
import backend.website.gbcc.config.property.JwtProperty;
import backend.website.gbcc.logic.analytics.dto.SiteAnalyticsCollectEventDto;
import backend.website.gbcc.logic.analytics.dto.SiteAnalyticsCollectRequestDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SiteAnalyticsCollectServiceImpl implements SiteAnalyticsCollectService {

    private final SiteAnalyticsEventRepository eventRepository;
    private final AnalyticsProperty analyticsProperty;
    private final JwtProperty jwtProperty;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public void collect(SiteAnalyticsCollectRequestDto request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        if (!analyticsProperty.isEnabled()) {
            return;
        }
        if (analyticsProperty.isHonorDnt() && "1".equals(httpRequest.getHeader("DNT"))) {
            return;
        }
        int maxBatch = analyticsProperty.getMaxBatchSize();
        if (request.events().size() > maxBatch) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Batch size exceeds limit of " + maxBatch);
        }

        Instant now = Instant.now();
        UUID visitorId = request.visitorId();
        UUID sessionId = request.sessionId();
        String userAgent = trimTo(httpRequest.getHeader("User-Agent"), analyticsProperty.getMaxUserAgentLength());

        List<SiteAnalyticsEventEntity> toSave = new ArrayList<>(request.events().size());
        for (SiteAnalyticsCollectEventDto ev : request.events()) {
            assertOccurredAtValid(ev.occurredAt(), now);
            String path = normalizePath(ev.path());
            String referrer = trimTo(ev.referrer(), analyticsProperty.getMaxReferrerLength());
            String eventType = trimTo(ev.eventType(), 32);
            if (eventType.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "eventType must not be empty");
            }
            if (eventType.length() > 32) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "eventType too long");
            }
            String metadataJson = validateAndSerializeMetadata(ev.metadata());

            SiteAnalyticsEventEntity row = new SiteAnalyticsEventEntity();
            row.setOccurredAt(ev.occurredAt());
            row.setReceivedAt(now);
            row.setVisitorId(visitorId);
            row.setSessionId(sessionId);
            row.setEventType(eventType);
            row.setPath(path);
            row.setReferrer(referrer);
            row.setUserAgent(userAgent);
            row.setMetadataJson(metadataJson);
            toSave.add(row);
        }

        eventRepository.saveAll(toSave);
        maybeSetVisitorCookie(httpRequest, httpResponse, visitorId);
    }

    private void assertOccurredAtValid(Instant occurredAt, Instant now) {
        Instant oldest = now.minus(analyticsProperty.getMaxEventAgeDays(), ChronoUnit.DAYS);
        if (occurredAt.isBefore(oldest)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "occurredAt is too far in the past");
        }
        Instant latest = now.plus(analyticsProperty.getMaxFutureSkewMinutes(), ChronoUnit.MINUTES);
        if (occurredAt.isAfter(latest)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "occurredAt is in the future");
        }
    }

    private String validateAndSerializeMetadata(java.util.Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return null;
        }
        try {
            String json = objectMapper.writeValueAsString(metadata);
            if (json.length() > analyticsProperty.getMaxMetadataChars()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "metadata exceeds max size");
            }
            return json;
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "metadata is not serializable");
        }
    }

    private String normalizePath(String path) {
        int max = analyticsProperty.getMaxPathLength();
        if (path == null || path.isBlank()) {
            return "/";
        }
        String t = path.trim();
        if (!t.startsWith("/")) {
            t = "/" + t;
        }
        return trimTo(t, max);
    }

    private static String trimTo(String value, int maxLen) {
        if (value == null) {
            return null;
        }
        String t = value.trim();
        if (t.isEmpty()) {
            return null;
        }
        if (t.length() <= maxLen) {
            return t;
        }
        return t.substring(0, maxLen);
    }

    private void maybeSetVisitorCookie(HttpServletRequest request, HttpServletResponse response, UUID visitorId) {
        if (!analyticsProperty.isCookieEnabled()) {
            return;
        }
        String cookieName = analyticsProperty.getCookieName();
        String existing = readCookieValue(request, cookieName);
        String expected = visitorId.toString();
        if (existing != null && existing.equalsIgnoreCase(expected)) {
            return;
        }
        boolean secure = analyticsProperty.getCookieSecure() != null
                ? analyticsProperty.getCookieSecure()
                : jwtProperty.isCookieSecure();
        ResponseCookie cookie = ResponseCookie.from(cookieName, expected)
                .httpOnly(true)
                .secure(secure)
                .sameSite(analyticsProperty.getCookieSameSite())
                .path("/")
                .maxAge(Duration.ofDays(analyticsProperty.getCookieTtlDays()))
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    private static String readCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie c : cookies) {
            if (name.equals(c.getName())) {
                return c.getValue();
            }
        }
        return null;
    }
}
