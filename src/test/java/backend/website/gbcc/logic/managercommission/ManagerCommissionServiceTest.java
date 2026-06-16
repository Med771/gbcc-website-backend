package backend.website.gbcc.logic.managercommission;

import backend.website.gbcc.config.property.ReferralProperty;
import backend.website.gbcc.logic.account.AccountEntity;
import backend.website.gbcc.logic.order.OrderEntity;
import backend.website.gbcc.logic.order.OrderRepository;
import backend.website.gbcc.model.AccountRole;
import backend.website.gbcc.model.OrderStatus;
import backend.website.gbcc.model.ReferralClientType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ManagerCommissionServiceTest {

    private static final UUID ORDER_ID = UUID.fromString("20000000-0000-4000-8000-000000000001");
    private static final UUID CUSTOMER_ID = UUID.fromString("10000000-0000-4000-8000-000000000001");
    private static final UUID MANAGER_ID = UUID.fromString("10000000-0000-4000-8000-000000000002");

    @Mock
    private ManagerReferralCommissionRepository managerReferralCommissionRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ReferralProperty referralProperty;
    @Mock
    private backend.website.gbcc.helper.SecurityContextHelper securityContextHelper;

    @InjectMocks
    private ManagerCommissionServiceImpl managerCommissionService;

    @Test
    void onOrderDelivered_firstDelivered_usesSevenPercent() {
        AccountEntity manager = admin(MANAGER_ID);
        AccountEntity buyer = customerWithManager(CUSTOMER_ID, manager);
        OrderEntity order = order(buyer, new BigDecimal("1000.00"));

        when(managerReferralCommissionRepository.existsByOrder_Id(ORDER_ID)).thenReturn(false);
        when(orderRepository.findByIdWithCustomerAndReferrer(ORDER_ID)).thenReturn(Optional.of(order));
        when(orderRepository.countByCustomer_IdAndStatus(CUSTOMER_ID, OrderStatus.DELIVERED)).thenReturn(1L);
        when(referralProperty.getCommissionPercentNewClient()).thenReturn(new BigDecimal("7"));

        managerCommissionService.onOrderDelivered(ORDER_ID);

        ArgumentCaptor<ManagerReferralCommissionEntity> cap = ArgumentCaptor.forClass(ManagerReferralCommissionEntity.class);
        verify(managerReferralCommissionRepository).save(cap.capture());
        assertThat(cap.getValue().getCommissionPercent()).isEqualByComparingTo("7");
        assertThat(cap.getValue().getCommissionAmount()).isEqualByComparingTo("70.00");
        assertThat(cap.getValue().getClientType()).isEqualTo(ReferralClientType.NEW);
    }

    @Test
    void onOrderDelivered_returningClient_usesTwoPercent() {
        AccountEntity manager = admin(MANAGER_ID);
        AccountEntity buyer = customerWithManager(CUSTOMER_ID, manager);
        OrderEntity order = order(buyer, new BigDecimal("500.00"));

        when(managerReferralCommissionRepository.existsByOrder_Id(ORDER_ID)).thenReturn(false);
        when(orderRepository.findByIdWithCustomerAndReferrer(ORDER_ID)).thenReturn(Optional.of(order));
        when(orderRepository.countByCustomer_IdAndStatus(CUSTOMER_ID, OrderStatus.DELIVERED)).thenReturn(2L);
        when(referralProperty.getCommissionPercentReturningClient()).thenReturn(new BigDecimal("2"));

        managerCommissionService.onOrderDelivered(ORDER_ID);

        ArgumentCaptor<ManagerReferralCommissionEntity> cap = ArgumentCaptor.forClass(ManagerReferralCommissionEntity.class);
        verify(managerReferralCommissionRepository).save(cap.capture());
        assertThat(cap.getValue().getCommissionPercent()).isEqualByComparingTo("2");
        assertThat(cap.getValue().getCommissionAmount()).isEqualByComparingTo("10.00");
        assertThat(cap.getValue().getClientType()).isEqualTo(ReferralClientType.RETURNING);
    }

    @Test
    void onOrderDelivered_skipsWithoutManager() {
        AccountEntity buyer = new AccountEntity();
        buyer.setId(CUSTOMER_ID);
        buyer.setRole(AccountRole.CUSTOMER);
        OrderEntity order = order(buyer, new BigDecimal("100.00"));

        when(managerReferralCommissionRepository.existsByOrder_Id(ORDER_ID)).thenReturn(false);
        when(orderRepository.findByIdWithCustomerAndReferrer(ORDER_ID)).thenReturn(Optional.of(order));

        managerCommissionService.onOrderDelivered(ORDER_ID);

        verify(managerReferralCommissionRepository, never()).save(any());
    }

    private static AccountEntity admin(UUID id) {
        AccountEntity e = new AccountEntity();
        e.setId(id);
        e.setRole(AccountRole.ADMIN);
        e.setIsBlocked(false);
        return e;
    }

    private static AccountEntity customerWithManager(UUID id, AccountEntity manager) {
        AccountEntity e = new AccountEntity();
        e.setId(id);
        e.setRole(AccountRole.CUSTOMER);
        e.setBroughtByManager(manager);
        return e;
    }

    private static OrderEntity order(AccountEntity buyer, BigDecimal total) {
        OrderEntity o = new OrderEntity();
        o.setId(ORDER_ID);
        o.setCustomer(buyer);
        o.setTotalDiscountedPrice(total);
        return o;
    }
}
