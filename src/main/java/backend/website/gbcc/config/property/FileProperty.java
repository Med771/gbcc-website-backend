package backend.website.gbcc.config.property;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.file")
@Data
public class FileProperty {
    private String bucket;
}
