package backend.website.gbcc.integration;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Публичные GET после Flyway — проверка маршрутов на типовых query-параметрах.
 */
@GbccPostgresIntegrationTest
@Tag("requires-docker")
class PublicReadEndpointsIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void productList_shouldReturn200() {
        ResponseEntity<String> res = restTemplate.getForEntity(
                "/product?page=0&size=5",
                String.class
        );
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void productClasses_shouldReturn200() {
        ResponseEntity<String> res = restTemplate.getForEntity("/product/classes", String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void newsCategories_shouldReturn200() {
        ResponseEntity<String> res = restTemplate.getForEntity("/news/categories", String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void promotionSearch_withExampleFilters_shouldReturn200() {
        ResponseEntity<String> res = restTemplate.getForEntity(
                "/promotion?page=0&size=10&name=Demo&isActive=true",
                String.class
        );
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
