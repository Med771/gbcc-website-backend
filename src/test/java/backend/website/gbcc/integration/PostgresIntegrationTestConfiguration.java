package backend.website.gbcc.integration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * PostgreSQL в Docker для интеграционных тестов: {@link ServiceConnection} подменяет
 * {@code spring.datasource.*} до старта Flyway/JPA (см. Spring Boot «Testcontainers»).
 */
@TestConfiguration(proxyBeanMethods = false)
public class PostgresIntegrationTestConfiguration {

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgresContainer() {
        return new PostgreSQLContainer(DockerImageName.parse("postgres:17"))
                .withDatabaseName("gbcc")
                .withUsername("GBCC")
                .withPassword("GBCC!05510");
    }
}
