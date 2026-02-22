package backend.website.gbcc.config;

import backend.website.gbcc.config.property.SwaggerProperty;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@RequiredArgsConstructor
public class SwaggerConfig {

    private final SwaggerProperty swaggerProperty;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(swaggerProperty.getTitle())
                        .description(swaggerProperty.getDescription())
                        .version(swaggerProperty.getVersion()))

                .servers(swaggerProperty.getServers().stream()
                        .map(serv -> new Server()
                                .url(serv.getUrl())
                                .description(serv.getDescription()))
                        .toList()
                );
    }
}
