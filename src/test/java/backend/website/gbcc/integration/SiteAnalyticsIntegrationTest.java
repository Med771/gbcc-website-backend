package backend.website.gbcc.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@GbccPostgresIntegrationTest
@Tag("requires-docker")
class SiteAnalyticsIntegrationTest {

    private static final String OWNER_EMAIL = "integration-owner@example.com";
    private static final String OWNER_PASSWORD = "OwnerIntegrationTest123!";

    @Autowired
    private TestRestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void collectThenCrmSummary_seesVisitor() throws Exception {
        UUID visitor = UUID.randomUUID();
        UUID session = UUID.randomUUID();
        String collectJson = """
                {
                  "visitorId": "%s",
                  "sessionId": "%s",
                  "events": [
                    {
                      "occurredAt": "%s",
                      "eventType": "PAGE_VIEW",
                      "path": "/integration-test",
                      "referrer": null,
                      "metadata": {"source": "it"}
                    }
                  ]
                }
                """.formatted(visitor, session, Instant.now().toString());

        ResponseEntity<String> collect = restTemplate.postForEntity(
                "/analytics/collect",
                new HttpEntity<>(collectJson, IntegrationTestHttp.jsonHeaders()),
                String.class
        );
        assertThat(collect.getStatusCode())
                .as("response body: %s", collect.getBody())
                .isEqualTo(HttpStatus.NO_CONTENT);

        String ownerCookie = loginCookie();
        Instant from = Instant.now().minus(1, ChronoUnit.HOURS);
        Instant to = Instant.now().plus(1, ChronoUnit.HOURS);
        String url = UriComponentsBuilder.fromPath("/crm/analytics/summary")
                .queryParam("from", from)
                .queryParam("to", to)
                .encode(StandardCharsets.UTF_8)
                .toUriString();

        ResponseEntity<String> summary = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(IntegrationTestHttp.jsonHeadersWithCookie(ownerCookie)),
                String.class
        );
        assertThat(summary.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode root = objectMapper.readTree(summary.getBody());
        assertThat(root.get("uniqueVisitors").asLong()).isGreaterThanOrEqualTo(1);
        assertThat(root.get("totalEvents").asLong()).isGreaterThanOrEqualTo(1);
    }

    private String loginCookie() {
        String loginJson = """
                {"email":"%s","password":"%s"}
                """.formatted(OWNER_EMAIL, OWNER_PASSWORD);
        ResponseEntity<String> login = restTemplate.postForEntity(
                "/auth/login",
                new HttpEntity<>(loginJson, IntegrationTestHttp.jsonHeaders()),
                String.class
        );
        assertThat(login.getStatusCode()).isEqualTo(HttpStatus.OK);
        var setCookies = login.getHeaders().get(HttpHeaders.SET_COOKIE);
        String access = IntegrationTestHttp.extractCookiePair(setCookies, "GBCC_ACCESS_TOKEN=");
        assertThat(access).isNotNull();
        return access;
    }
}
