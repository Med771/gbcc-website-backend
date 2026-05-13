package backend.website.gbcc.logic.analytics;

import backend.website.gbcc.logic.analytics.dto.SiteAnalyticsCollectRequestDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface SiteAnalyticsCollectService {

    void collect(SiteAnalyticsCollectRequestDto request, HttpServletRequest httpRequest, HttpServletResponse httpResponse);
}
