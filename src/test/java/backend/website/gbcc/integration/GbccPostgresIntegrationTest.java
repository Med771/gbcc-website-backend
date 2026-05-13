package backend.website.gbcc.integration;

import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Полный контекст приложения, профиль {@code test}, PostgreSQL через Testcontainers,
 * HTTP-клиент {@link org.springframework.boot.resttestclient.TestRestTemplate} (Spring Boot 4).
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(PostgresIntegrationTestConfiguration.class)
@AutoConfigureTestRestTemplate
public @interface GbccPostgresIntegrationTest {
}
