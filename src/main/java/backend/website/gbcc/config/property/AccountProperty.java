package backend.website.gbcc.config.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.account")
public class AccountProperty {
    private OwnerProperty owner = new OwnerProperty();

    @Data
    public static class OwnerProperty {
        private String email;
        private String password;
    }
}
