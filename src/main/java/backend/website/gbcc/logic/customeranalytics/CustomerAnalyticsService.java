package backend.website.gbcc.logic.customeranalytics;

import backend.website.gbcc.logic.customeranalytics.dto.CustomerAnalyticsResponseDto;
import backend.website.gbcc.logic.customeranalytics.dto.UpdateCustomerAnalyticsRequestDto;

import java.util.UUID;

public interface CustomerAnalyticsService {

    CustomerAnalyticsResponseDto getForCustomer(UUID customerAccountId);

    CustomerAnalyticsResponseDto updateManual(UUID customerAccountId, UpdateCustomerAnalyticsRequestDto requestDto);
}
