package backend.website.gbcc.config.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Data
@Component
@ConfigurationProperties(prefix = "app.referral")
public class ReferralProperty {

    private String linkBaseUrl = "http://localhost:3000/ref";

    /** @deprecated use commissionPercentNewClient / commissionPercentReturningClient */
    @Deprecated
    private BigDecimal commissionPercent = new BigDecimal("5");

    private BigDecimal commissionPercentNewClient = new BigDecimal("7");

    private BigDecimal commissionPercentReturningClient = new BigDecimal("2");

    private BigDecimal minWithdrawalAmount = new BigDecimal("1000");
}
