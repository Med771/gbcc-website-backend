package backend.website.gbcc.logic.customeranalytics;

import backend.website.gbcc.helper.SecurityContextHelper;
import backend.website.gbcc.logic.account.AccountEntity;
import backend.website.gbcc.logic.account.AccountRepository;
import backend.website.gbcc.logic.customeranalytics.dto.UpdateCustomerAnalyticsRequestDto;
import backend.website.gbcc.logic.order.OrderRepository;
import backend.website.gbcc.model.AccountPrincipal;
import backend.website.gbcc.model.AccountRole;
import backend.website.gbcc.model.OrderStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerAnalyticsServiceTest {

    @Mock
    private CustomerAnalyticsRepository customerAnalyticsRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private SecurityContextHelper securityContextHelper;

    @InjectMocks
    private CustomerAnalyticsServiceImpl customerAnalyticsService;

    @Test
    void getForCustomer_mergesManualAndComputed() {
        UUID ownerId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        AccountEntity owner = new AccountEntity();
        owner.setId(ownerId);
        owner.setRole(AccountRole.OWNER);

        AccountEntity customer = new AccountEntity();
        customer.setId(customerId);
        customer.setRole(AccountRole.CUSTOMER);

        when(securityContextHelper.getCurrentAccountIdOrThrow()).thenReturn(ownerId);
        when(accountRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(accountRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(customerAnalyticsRepository.findById(customerId)).thenReturn(Optional.empty());
        when(orderRepository.sumTotalDiscountedPriceByCustomerIdAndStatus(customerId, OrderStatus.DELIVERED))
                .thenReturn(new BigDecimal("1500.00"));
        when(orderRepository.sumTotalDiscountedPriceByCustomerIdAndStatusAndCreatedAtBetween(
                any(), any(), any(), any())).thenReturn(new BigDecimal("200.00"));
        when(orderRepository.countByCustomer_IdAndStatus(customerId, OrderStatus.DELIVERED)).thenReturn(3L);
        when(accountRepository.countByReferredBy_Id(customerId)).thenReturn(2L);

        var dto = customerAnalyticsService.getForCustomer(customerId);

        assertThat(dto.computedTotalPaid()).isEqualByComparingTo("1500.00");
        assertThat(dto.computedTotalOrdersCount()).isEqualTo(3);
        assertThat(dto.computedReferredClientsCount()).isEqualTo(2);
    }

    @Test
    void updateManual_persistsFields() {
        UUID ownerId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        AccountEntity owner = new AccountEntity();
        owner.setId(ownerId);
        owner.setRole(AccountRole.OWNER);

        AccountEntity customer = new AccountEntity();
        customer.setId(customerId);
        customer.setRole(AccountRole.CUSTOMER);

        AtomicReference<CustomerAnalyticsEntity> stored = new AtomicReference<>();

        when(securityContextHelper.requireAdminOrOwner(any())).thenReturn(new AccountPrincipal(ownerId, AccountRole.OWNER));
        when(securityContextHelper.getCurrentAccountIdOrThrow()).thenReturn(ownerId);
        when(accountRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(accountRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(customerAnalyticsRepository.findById(customerId)).thenAnswer(inv -> Optional.ofNullable(stored.get()));
        when(customerAnalyticsRepository.save(any())).thenAnswer(inv -> {
            CustomerAnalyticsEntity row = inv.getArgument(0);
            stored.set(row);
            return row;
        });
        when(orderRepository.sumTotalDiscountedPriceByCustomerIdAndStatus(customerId, OrderStatus.DELIVERED))
                .thenReturn(BigDecimal.ZERO);
        when(orderRepository.sumTotalDiscountedPriceByCustomerIdAndStatusAndCreatedAtBetween(
                any(), any(), any(), any())).thenReturn(BigDecimal.ZERO);
        when(orderRepository.countByCustomer_IdAndStatus(customerId, OrderStatus.DELIVERED)).thenReturn(0L);
        when(accountRepository.countByReferredBy_Id(customerId)).thenReturn(0L);

        var dto = customerAnalyticsService.updateManual(
                customerId,
                new UpdateCustomerAnalyticsRequestDto(
                        new BigDecimal("999.99"),
                        null,
                        5,
                        null
                )
        );

        assertThat(dto.manualTotalPaid()).isEqualByComparingTo("999.99");
        assertThat(dto.manualTotalOrdersCount()).isEqualTo(5);
    }
}
