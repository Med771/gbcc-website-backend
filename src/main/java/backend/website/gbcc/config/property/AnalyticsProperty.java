package backend.website.gbcc.config.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.analytics")
public class AnalyticsProperty {

    private boolean enabled = true;

    /** HttpOnly cookie: имя для visitor id (first-party). */
    private String cookieName = "GBCC_VISITOR_ID";

    private int cookieTtlDays = 400;

    /** Если true — cookie только по HTTPS (как app.jwt.cookie-secure). */
    private Boolean cookieSecure;

    private String cookieSameSite = "Lax";

    private int maxBatchSize = 50;

    private int maxPathLength = 1024;

    private int maxReferrerLength = 1024;

    private int maxUserAgentLength = 512;

    /** Максимальный размер JSON metadata в символах (после сериализации). */
    private int maxMetadataChars = 4000;

    private int topPathsLimit = 15;

    /** Не принимать события старше этого окна (дней) относительно сервера. */
    private int maxEventAgeDays = 7;

    /** Допустимое «будущее» события относительно сервера (минуты). */
    private int maxFutureSkewMinutes = 5;

    private boolean honorDnt = true;

    /** Выставлять Set-Cookie с visitor id при рассинхроне или первом визите. */
    private boolean cookieEnabled = true;
}
