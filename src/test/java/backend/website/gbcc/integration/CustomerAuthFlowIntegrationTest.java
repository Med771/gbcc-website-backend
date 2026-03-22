package backend.website.gbcc.integration;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Полный контекст + PostgreSQL в Docker. Тег {@code requires-docker} исключён из default {@code mvn test}.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
@Tag("requires-docker")
class CustomerAuthFlowIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void registerAndLogin_shouldReturn200AndSetCookies() {
        String email = "cust-" + UUID.randomUUID() + "@example.com";
        String registerBody = """
                {
                  "name": "Integration User",
                  "phone": "+10000000001",
                  "email": "%s",
                  "password": "Password123!"
                }
                """.formatted(email);

        HttpHeaders jsonHeaders = new HttpHeaders();
        jsonHeaders.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> reg = restTemplate.postForEntity(
                "/account/customer/register",
                new HttpEntity<>(registerBody, jsonHeaders),
                String.class
        );
        assertThat(reg.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        String loginBody = """
                {
                  "email": "%s",
                  "password": "Password123!"
                }
                """.formatted(email);

        ResponseEntity<String> login = restTemplate.postForEntity(
                "/auth/login",
                new HttpEntity<>(loginBody, jsonHeaders),
                String.class
        );
        assertThat(login.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(login.getHeaders().get("Set-Cookie")).anyMatch(v -> v != null && v.contains("GBCC_ACCESS_TOKEN"));
    }

    @Test
    void registerDuplicateEmail_shouldReturnConflict() {
        String email = "dup-" + UUID.randomUUID() + "@example.com";
        String body = """
                {
                  "name": "A",
                  "phone": "+10000000002",
                  "email": "%s",
                  "password": "Password123!"
                }
                """.formatted(email);

        HttpHeaders jsonHeaders = new HttpHeaders();
        jsonHeaders.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> first = restTemplate.postForEntity(
                "/account/customer/register",
                new HttpEntity<>(body, jsonHeaders),
                String.class
        );
        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<String> second = restTemplate.postForEntity(
                "/account/customer/register",
                new HttpEntity<>(body, jsonHeaders),
                String.class
        );
        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void flyway_shouldHaveAppliedMigrations() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM flyway_schema_history",
                Integer.class
        );
        assertThat(count).isNotNull().isPositive();
    }
}
