package backend.website.gbcc.config.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Data
@Component
@ConfigurationProperties(prefix = "app.referral")
public class ReferralProperty {

    /**
     * Базовый URL ссылки без завершающего слэша, например <a href="https://site.ru/ref">...</a> — клиент дополняет /{code}.
     */
    private String linkBaseUrl = "http://localhost:3000/ref";

    private BigDecimal commissionPercent = new BigDecimal("5");

    private BigDecimal minWithdrawalAmount = new BigDecimal("1000");
}
