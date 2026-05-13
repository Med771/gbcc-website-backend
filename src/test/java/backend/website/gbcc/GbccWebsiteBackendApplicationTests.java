package backend.website.gbcc;

import backend.website.gbcc.integration.PostgresIntegrationTestConfiguration;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(PostgresIntegrationTestConfiguration.class)
@Tag("requires-docker")
class GbccWebsiteBackendApplicationTests {

    @Test
    void contextLoads() {
    }
}
