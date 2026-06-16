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
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Сквозной сценарий: создание товара с JWT владельца (OWNER/ADMIN) → регистрация клиента → заказ → смена статусов OWNER до DELIVERED;
 * отдельно — отмена с CREATED и запрет дальнейшей смены статуса.
 */
@GbccPostgresIntegrationTest
@Tag("requires-docker")
class OrderLifecycleIntegrationTest {

    private static final String OWNER_EMAIL = "integration-owner@example.com";
    private static final String OWNER_PASSWORD = "OwnerIntegrationTest123!";

    @Autowired
    private TestRestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void customerOrder_happyPathThroughDelivered() throws Exception {
        String ownerCookie = loginCookie(OWNER_EMAIL, OWNER_PASSWORD);

        String brand = "E2E-" + UUID.randomUUID();
        String productJson = """
                {
                  "className": "E2EClass",
                  "seriesName": "E2ESeries",
                  "brand": "%s",
                  "description": "d",
                  "tagline": null,
                  "tags": null,
                  "deliveryText": null,
                  "licensesText": null,
                  "heightMm": 100,
                  "widthMm": 100,
                  "lengthMm": 100,
                  "weightKg": 1.0,
                  "price": 99.99,
                  "discountPercent": 0,
                  "isActive": true,
                  "stockQuantity": 10
                }
                """.formatted(brand);

        ResponseEntity<String> productRes = restTemplate.postForEntity(
                "/product",
                new HttpEntity<>(productJson, IntegrationTestHttp.jsonHeadersWithCookie(ownerCookie)),
                String.class
        );
        assertThat(productRes.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        JsonNode productNode = objectMapper.readTree(productRes.getBody());
        String productId = productNode.get("id").asText();

        String email = "ord-" + UUID.randomUUID() + "@example.com";
        String phone = String.format("+79%09d", ThreadLocalRandom.current().nextInt(100_000_000, 1_000_000_000));
        String registerJson = """
                {
                  "firstName": "Order",
                  "lastName": "Customer",
                  "patronymic": null,
                  "phone": "%s",
                  "email": "%s",
                  "password": "Password123!",
                  "inviteCode": null
                }
                """.formatted(phone, email);

        ResponseEntity<String> reg = restTemplate.postForEntity(
                "/account/customer/register",
                new HttpEntity<>(registerJson, IntegrationTestHttp.jsonHeaders()),
                String.class
        );
        assertThat(reg.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        String customerCookie = loginCookie(email, "Password123!");

        String orderJson = """
                {
                  "deliveryAddress": "Test address 1",
                  "customerComment": null,
                  "paymentMethod": "CARD_ONLINE",
                  "items": [ {"productId": "%s", "quantity": 1} ]
                }
                """.formatted(productId);

        ResponseEntity<String> orderRes = restTemplate.postForEntity(
                "/order",
                new HttpEntity<>(orderJson, IntegrationTestHttp.jsonHeadersWithCookie(customerCookie)),
                String.class
        );
        assertThat(orderRes.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        JsonNode orderNode = objectMapper.readTree(orderRes.getBody());
        String orderId = orderNode.get("id").asText();
        assertThat(orderNode.get("status").asText()).isEqualTo("CREATED");

        patchStatus(ownerCookie, orderId, "PROCESSING", """
                {"status":"PROCESSING","estimatedDeliveryAt":"2026-06-01T10:00:00Z","estimatedDeliveryEnd":"2026-06-02T10:00:00Z","receiptUrl":null,"comment":"go"}""");
        patchStatus(ownerCookie, orderId, "PACKED", patchMinimal("PACKED"));
        patchStatus(ownerCookie, orderId, "SHIPPED", patchMinimal("SHIPPED"));
        patchStatus(ownerCookie, orderId, "DELIVERED", patchMinimal("DELIVERED"));

        ResponseEntity<String> getRes = restTemplate.exchange(
                "/order/" + orderId,
                HttpMethod.GET,
                new HttpEntity<>(IntegrationTestHttp.jsonHeadersWithCookie(customerCookie)),
                String.class
        );
        assertThat(getRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode finalOrder = objectMapper.readTree(getRes.getBody());
        assertThat(finalOrder.get("status").asText()).isEqualTo("DELIVERED");

        Integer historyRows = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM order_status_history WHERE order_id = ?",
                Integer.class,
                UUID.fromString(orderId)
        );
        assertThat(historyRows).isNotNull().isGreaterThanOrEqualTo(5);
    }

    @Test
    void customerOrder_cancelFromCreated_blocksFurtherStatusChange() throws Exception {
        String ownerCookie = loginCookie(OWNER_EMAIL, OWNER_PASSWORD);

        String brand = "E2E-C-" + UUID.randomUUID();
        String productJson = """
                {
                  "className": "E2EClassC",
                  "seriesName": "E2ESeriesC",
                  "brand": "%s",
                  "description": "d",
                  "tagline": null,
                  "tags": null,
                  "deliveryText": null,
                  "licensesText": null,
                  "heightMm": 100,
                  "widthMm": 100,
                  "lengthMm": 100,
                  "weightKg": 1.0,
                  "price": 10.00,
                  "discountPercent": 0,
                  "isActive": true,
                  "stockQuantity": 10
                }
                """.formatted(brand);

        ResponseEntity<String> productRes = restTemplate.postForEntity(
                "/product",
                new HttpEntity<>(productJson, IntegrationTestHttp.jsonHeadersWithCookie(ownerCookie)),
                String.class
        );
        assertThat(productRes.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String productId = objectMapper.readTree(productRes.getBody()).get("id").asText();

        String email = "canc-" + UUID.randomUUID() + "@example.com";
        String phone = String.format("+78%09d", ThreadLocalRandom.current().nextInt(100_000_000, 1_000_000_000));
        String registerJson = """
                {
                  "firstName": "C",
                  "lastName": "User",
                  "patronymic": null,
                  "phone": "%s",
                  "email": "%s",
                  "password": "Password123!",
                  "inviteCode": null
                }
                """.formatted(phone, email);

        assertThat(restTemplate.postForEntity(
                "/account/customer/register",
                new HttpEntity<>(registerJson, IntegrationTestHttp.jsonHeaders()),
                String.class
        ).getStatusCode()).isEqualTo(HttpStatus.CREATED);

        String customerCookie = loginCookie(email, "Password123!");

        String orderJson = """
                {
                  "deliveryAddress": "Addr",
                  "customerComment": null,
                  "paymentMethod": "CARD_OR_ON_RECEIPT",
                  "items": [ {"productId": "%s", "quantity": 1} ]
                }
                """.formatted(productId);

        ResponseEntity<String> orderRes = restTemplate.postForEntity(
                "/order",
                new HttpEntity<>(orderJson, IntegrationTestHttp.jsonHeadersWithCookie(customerCookie)),
                String.class
        );
        assertThat(orderRes.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String orderId = objectMapper.readTree(orderRes.getBody()).get("id").asText();

        patchStatus(ownerCookie, orderId, "CANCELLED", patchMinimal("CANCELLED"));

        ResponseEntity<String> bad = restTemplate.exchange(
                "/order/" + orderId + "/status",
                HttpMethod.PATCH,
                new HttpEntity<>(patchMinimal("PROCESSING"), IntegrationTestHttp.jsonHeadersWithCookie(ownerCookie)),
                String.class
        );
        assertThat(bad.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private void patchStatus(String ownerCookie, String orderId, String expectedStatus, String jsonBody)
            throws Exception {
        ResponseEntity<String> res = restTemplate.exchange(
                "/order/" + orderId + "/status",
                HttpMethod.PATCH,
                new HttpEntity<>(jsonBody, IntegrationTestHttp.jsonHeadersWithCookie(ownerCookie)),
                String.class
        );
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(objectMapper.readTree(res.getBody()).get("status").asText()).isEqualTo(expectedStatus);
    }

    private static String patchMinimal(String status) {
        return """
                {"status":"%s","estimatedDeliveryAt":null,"estimatedDeliveryEnd":null,"receiptUrl":null,"comment":null}
                """.formatted(status).trim();
    }

    private String loginCookie(String email, String password) {
        String loginJson = """
                {"email":"%s","password":"%s"}
                """.formatted(email, password);
        ResponseEntity<String> login = restTemplate.postForEntity(
                "/auth/login",
                new HttpEntity<>(loginJson, IntegrationTestHttp.jsonHeaders()),
                String.class
        );
        assertThat(login.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<String> setCookies = login.getHeaders().get(HttpHeaders.SET_COOKIE);
        String access = IntegrationTestHttp.extractCookiePair(setCookies, "GBCC_ACCESS_TOKEN=");
        assertThat(access).isNotNull();
        return access;
    }
}
