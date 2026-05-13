package backend.website.gbcc.integration;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static backend.website.gbcc.integration.fixtures.IntegrationExampleRequests.customerRegisterJson;
import static backend.website.gbcc.integration.fixtures.IntegrationExampleRequests.loginJson;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Полный контекст + PostgreSQL в Docker (Testcontainers, см. {@link PostgresIntegrationTestConfiguration}).
 * Нужен запущенный Docker (например Docker Desktop).
 */
@GbccPostgresIntegrationTest
@Tag("requires-docker")
class CustomerAuthFlowIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void registerAndLogin_shouldReturn200AndSetCookies() {
        String email = "cust-" + UUID.randomUUID() + "@example.com";
        String phone = String.format("+79%09d", ThreadLocalRandom.current().nextInt(100_000_000, 1_000_000_000));
        String registerBody = customerRegisterJson(phone, email);

        HttpHeaders jsonHeaders = new HttpHeaders();
        jsonHeaders.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> reg = restTemplate.postForEntity(
                "/account/customer/register",
                new HttpEntity<>(registerBody, jsonHeaders),
                String.class
        );
        assertThat(reg.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        String loginBody = loginJson(email, "Password123!");

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
        String phone = String.format("+79%09d", ThreadLocalRandom.current().nextInt(100_000_000, 1_000_000_000));
        String body = customerRegisterJson(phone, email);

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
