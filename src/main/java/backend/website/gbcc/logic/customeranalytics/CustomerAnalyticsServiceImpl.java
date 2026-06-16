package backend.website.gbcc.logic.customeranalytics;

import backend.website.gbcc.helper.SecurityContextHelper;
import backend.website.gbcc.logic.account.AccountEntity;
import backend.website.gbcc.logic.account.AccountRepository;
import backend.website.gbcc.logic.customeranalytics.dto.CustomerAnalyticsResponseDto;
import backend.website.gbcc.logic.customeranalytics.dto.UpdateCustomerAnalyticsRequestDto;
import backend.website.gbcc.logic.order.OrderRepository;
import backend.website.gbcc.model.AccountRole;
import backend.website.gbcc.model.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerAnalyticsServiceImpl implements CustomerAnalyticsService {

    private final CustomerAnalyticsRepository customerAnalyticsRepository;
    private final AccountRepository accountRepository;
    private final OrderRepository orderRepository;
    private final SecurityContextHelper securityContextHelper;

    @Override
    @Transactional(readOnly = true)
    public CustomerAnalyticsResponseDto getForCustomer(UUID customerAccountId) {
        AccountEntity customer = findCustomerOrThrow(customerAccountId);
        ensureCanAccessCustomerAnalytics(customer);
        return buildResponse(customer);
    }

    @Override
    @Transactional
    public CustomerAnalyticsResponseDto updateManual(UUID customerAccountId, UpdateCustomerAnalyticsRequestDto requestDto) {
        securityContextHelper.requireAdminOrOwner("Only admin or owner can update customer analytics");
        AccountEntity customer = findCustomerOrThrow(customerAccountId);
        UUID editorId = securityContextHelper.getCurrentAccountIdOrThrow();
        AccountEntity editor = accountRepository.findById(editorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account not found"));

        CustomerAnalyticsEntity row = customerAnalyticsRepository.findById(customerAccountId)
                .orElseGet(() -> {
                    CustomerAnalyticsEntity created = new CustomerAnalyticsEntity();
                    created.setAccountId(customerAccountId);
                    created.setAccount(customer);
                    return created;
                });

        if (requestDto.manualTotalPaid() != null) {
            row.setManualTotalPaid(requestDto.manualTotalPaid());
        }
        if (requestDto.manualCurrentMonthPaid() != null) {
            row.setManualCurrentMonthPaid(requestDto.manualCurrentMonthPaid());
        }
        if (requestDto.manualTotalOrdersCount() != null) {
            row.setManualTotalOrdersCount(requestDto.manualTotalOrdersCount());
        }
        if (requestDto.manualReferredClientsCount() != null) {
            row.setManualReferredClientsCount(requestDto.manualReferredClientsCount());
        }
        row.setUpdatedAt(Instant.now());
        row.setUpdatedByAccount(editor);
        customerAnalyticsRepository.save(row);
        return buildResponse(customer);
    }

    private CustomerAnalyticsResponseDto buildResponse(AccountEntity customer) {
        UUID customerId = customer.getId();
        CustomerAnalyticsEntity manual = customerAnalyticsRepository.findById(customerId).orElse(null);

        BigDecimal computedTotalPaid = orderRepository.sumTotalDiscountedPriceByCustomerIdAndStatus(
                customerId, OrderStatus.DELIVERED
        );
        YearMonth currentMonth = YearMonth.now(ZoneOffset.UTC);
        Instant monthStart = currentMonth.atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant monthEnd = currentMonth.plusMonths(1).atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        BigDecimal computedCurrentMonthPaid = orderRepository.sumTotalDiscountedPriceByCustomerIdAndStatusAndCreatedAtBetween(
                customerId, OrderStatus.DELIVERED, monthStart, monthEnd
        );
        long computedTotalOrdersCount = orderRepository.countByCustomer_IdAndStatus(customerId, OrderStatus.DELIVERED);
        long computedReferredClientsCount = accountRepository.countByReferredBy_Id(customerId);

        return new CustomerAnalyticsResponseDto(
                customerId,
                manual != null ? manual.getManualTotalPaid() : null,
                manual != null ? manual.getManualCurrentMonthPaid() : null,
                manual != null ? manual.getManualTotalOrdersCount() : null,
                manual != null ? manual.getManualReferredClientsCount() : null,
                computedTotalPaid,
                computedCurrentMonthPaid,
                computedTotalOrdersCount,
                computedReferredClientsCount,
                manual != null ? manual.getUpdatedAt() : null,
                manual != null && manual.getUpdatedByAccount() != null ? manual.getUpdatedByAccount().getId() : null
        );
    }

    private void ensureCanAccessCustomerAnalytics(AccountEntity customer) {
        UUID requesterId = securityContextHelper.getCurrentAccountIdOrThrow();
        AccountEntity requester = accountRepository.findById(requesterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account not found"));
        if (requester.getRole() == AccountRole.ADMIN || requester.getRole() == AccountRole.OWNER) {
            return;
        }
        if (requester.getRole() == AccountRole.CUSTOMER && requester.getId().equals(customer.getId())) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
    }

    private AccountEntity findCustomerOrThrow(UUID customerAccountId) {
        AccountEntity account = accountRepository.findById(customerAccountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
        if (account.getRole() != AccountRole.CUSTOMER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Account is not a customer");
        }
        return account;
    }
}
