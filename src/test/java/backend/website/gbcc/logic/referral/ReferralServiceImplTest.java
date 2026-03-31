package backend.website.gbcc.logic.referral;

import backend.website.gbcc.config.property.ReferralProperty;
import backend.website.gbcc.helper.SecurityContextHelper;
import backend.website.gbcc.logic.account.AccountEntity;
import backend.website.gbcc.logic.account.AccountRepository;
import backend.website.gbcc.logic.order.OrderEntity;
import backend.website.gbcc.logic.order.OrderRepository;
import backend.website.gbcc.logic.referral.dto.ReferralApplyInviteCodeRequestDto;
import backend.website.gbcc.logic.referral.dto.ReferralClickRequestDto;
import backend.website.gbcc.logic.referral.dto.ReferralMeResponseDto;
import backend.website.gbcc.logic.referral.dto.ReferralWithdrawRequestDto;
import backend.website.gbcc.model.AccountRegistrationStatus;
import backend.website.gbcc.model.AccountRole;
import backend.website.gbcc.model.ReferralWithdrawalStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReferralServiceImplTest {

    private static final UUID CUSTOMER_ID = UUID.fromString("10000000-0000-4000-8000-000000000001");
    private static final UUID INVITER_ID = UUID.fromString("10000000-0000-4000-8000-000000000002");
    private static final UUID ORDER_ID = UUID.fromString("20000000-0000-4000-8000-000000000001");

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ReferralClickRepository referralClickRepository;
    @Mock
    private ReferralCommissionRepository referralCommissionRepository;
    @Mock
    private ReferralWithdrawalRepository referralWithdrawalRepository;
    @Mock
    private ReferralProperty referralProperty;
    @Mock
    private ReferralCodeGenerator referralCodeGenerator;
    @Mock
    private SecurityContextHelper securityContextHelper;

    @InjectMocks
    private ReferralServiceImpl referralService;

    @BeforeEach
    void wireReferralDefaults() {
        lenient().when(referralProperty.getCommissionPercent()).thenReturn(new BigDecimal("5"));
        lenient().when(referralProperty.getMinWithdrawalAmount()).thenReturn(new BigDecimal("1000"));
        lenient().when(referralProperty.getLinkBaseUrl()).thenReturn("https://site.ru/ref");
    }

    @Test
    void recordClick_savesWhenValidCustomerReferrer() {
        AccountEntity referrer = customerWithCode(INVITER_ID, "AB2-CD3F");
        when(accountRepository.findByReferralCode("AB2-CD3F")).thenReturn(Optional.of(referrer));

        referralService.recordClick(new ReferralClickRequestDto("#ab2cd3f", "fp1"));

        ArgumentCaptor<ReferralClickEntity> cap = ArgumentCaptor.forClass(ReferralClickEntity.class);
        verify(referralClickRepository).save(cap.capture());
        assertThat(cap.getValue().getReferrerAccount()).isSameAs(referrer);
        assertThat(cap.getValue().getVisitorFingerprint()).isEqualTo("fp1");
    }

    @Test
    void recordClick_invalidFormat_throwsBadRequest() {
        assertThatThrownBy(() -> referralService.recordClick(new ReferralClickRequestDto("xx", null)))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode().value()).isEqualTo(400));
        verify(referralClickRepository, never()).save(any());
    }

    @Test
    void recordClick_blockedReferrer_throwsNotFound() {
        AccountEntity referrer = customerWithCode(INVITER_ID, "AB2-CD3F");
        referrer.setIsBlocked(true);
        when(accountRepository.findByReferralCode("AB2-CD3F")).thenReturn(Optional.of(referrer));

        assertThatThrownBy(() -> referralService.recordClick(new ReferralClickRequestDto("AB2-CD3F", null)))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode().value()).isEqualTo(404));
    }

    @Test
    void bindInviterForNewCustomer_blank_doesNothing() {
        AccountEntity newbie = new AccountEntity();
        referralService.bindInviterForNewCustomer(newbie, "   ");
        assertThat(newbie.getReferredBy()).isNull();
        verify(accountRepository, never()).findByReferralCode(any());
    }

    @Test
    void bindInviterForNewCustomer_setsReferredBy() {
        AccountEntity inviter = customerWithCode(INVITER_ID, "ZZ1-AA99");
        when(accountRepository.findByReferralCode("ZZ1-AA99")).thenReturn(Optional.of(inviter));

        AccountEntity newbie = new AccountEntity();
        referralService.bindInviterForNewCustomer(newbie, "zz1-aa99");

        assertThat(newbie.getReferredBy()).isSameAs(inviter);
    }

    @Test
    void assignReferralCodeIfMissing_generatesForCustomer() {
        AccountEntity customer = new AccountEntity();
        customer.setId(CUSTOMER_ID);
        customer.setRole(AccountRole.CUSTOMER);
        customer.setReferralCode(null);
        when(accountRepository.findById(CUSTOMER_ID)).thenReturn(Optional.of(customer));
        when(referralCodeGenerator.generateUniqueCode(any())).thenReturn("QQ1-WW88");
        when(accountRepository.save(customer)).thenReturn(customer);

        referralService.assignReferralCodeIfMissing(CUSTOMER_ID);

        assertThat(customer.getReferralCode()).isEqualTo("QQ1-WW88");
        verify(accountRepository).save(customer);
    }

    @Test
    void assignReferralCodeIfMissing_skipsAdmin() {
        AccountEntity admin = new AccountEntity();
        admin.setId(CUSTOMER_ID);
        admin.setRole(AccountRole.ADMIN);
        when(accountRepository.findById(CUSTOMER_ID)).thenReturn(Optional.of(admin));

        referralService.assignReferralCodeIfMissing(CUSTOMER_ID);

        verify(referralCodeGenerator, never()).generateUniqueCode(any());
        verify(accountRepository, never()).save(admin);
    }

    @Test
    void applyInviteCode_conflictWhenAlreadyBound() {
        AccountEntity inviter = customerWithCode(INVITER_ID, "AA1-BB22");
        AccountEntity me = new AccountEntity();
        me.setId(CUSTOMER_ID);
        me.setRole(AccountRole.CUSTOMER);
        me.setReferredBy(inviter);

        when(securityContextHelper.getCurrentAccountIdOrThrow()).thenReturn(CUSTOMER_ID);
        when(accountRepository.findById(CUSTOMER_ID)).thenReturn(Optional.of(me));

        assertThatThrownBy(() -> referralService.applyInviteCode(new ReferralApplyInviteCodeRequestDto("CC3-DD44")))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode().value()).isEqualTo(409));
    }

    @Test
    void applyInviteCode_rejectsSelf() {
        AccountEntity me = customerWithCode(CUSTOMER_ID, "ME1-SELF");
        me.setRole(AccountRole.CUSTOMER);
        me.setReferredBy(null);

        when(securityContextHelper.getCurrentAccountIdOrThrow()).thenReturn(CUSTOMER_ID);
        when(accountRepository.findById(CUSTOMER_ID)).thenReturn(Optional.of(me));
        when(accountRepository.findByReferralCode("ME1-SELF")).thenReturn(Optional.of(me));

        assertThatThrownBy(() -> referralService.applyInviteCode(new ReferralApplyInviteCodeRequestDto("ME1-SELF")))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode().value()).isEqualTo(400));
    }

    @Test
    void onOrderDelivered_createsCommission() {
        AccountEntity referrer = customerWithCode(INVITER_ID, "R01-RF99");
        AccountEntity buyer = new AccountEntity();
        buyer.setId(CUSTOMER_ID);
        buyer.setRole(AccountRole.CUSTOMER);
        buyer.setReferredBy(referrer);

        OrderEntity order = new OrderEntity();
        order.setId(ORDER_ID);
        order.setCustomer(buyer);
        order.setTotalDiscountedPrice(new BigDecimal("200.00"));

        when(referralCommissionRepository.existsByOrder_Id(ORDER_ID)).thenReturn(false);
        when(orderRepository.findByIdWithCustomerAndReferrer(ORDER_ID)).thenReturn(Optional.of(order));

        referralService.onOrderDelivered(ORDER_ID);

        ArgumentCaptor<ReferralCommissionEntity> cap = ArgumentCaptor.forClass(ReferralCommissionEntity.class);
        verify(referralCommissionRepository).save(cap.capture());
        assertThat(cap.getValue().getCommissionAmount()).isEqualByComparingTo(new BigDecimal("10.00"));
        assertThat(cap.getValue().getReferrerAccount()).isSameAs(referrer);
        assertThat(cap.getValue().getRefereeAccount()).isSameAs(buyer);
    }

    @Test
    void onOrderDelivered_skipsWithoutReferrer() {
        AccountEntity buyer = new AccountEntity();
        buyer.setId(CUSTOMER_ID);
        buyer.setRole(AccountRole.CUSTOMER);
        buyer.setReferredBy(null);

        OrderEntity order = new OrderEntity();
        order.setId(ORDER_ID);
        order.setCustomer(buyer);

        when(referralCommissionRepository.existsByOrder_Id(ORDER_ID)).thenReturn(false);
        when(orderRepository.findByIdWithCustomerAndReferrer(ORDER_ID)).thenReturn(Optional.of(order));

        referralService.onOrderDelivered(ORDER_ID);

        verify(referralCommissionRepository, never()).save(any());
    }

    @Test
    void requestWithdrawal_belowMinimum_throws() {
        AccountEntity me = customerWithCode(CUSTOMER_ID, "X01-YZ99");
        me.setRole(AccountRole.CUSTOMER);

        when(securityContextHelper.getCurrentAccountIdOrThrow()).thenReturn(CUSTOMER_ID);
        when(accountRepository.findById(CUSTOMER_ID)).thenReturn(Optional.of(me));
        when(referralCommissionRepository.sumCommissionByReferrer(CUSTOMER_ID)).thenReturn(new BigDecimal("5000"));
        when(referralWithdrawalRepository.sumAmountByAccountAndStatus(CUSTOMER_ID, ReferralWithdrawalStatus.PENDING))
                .thenReturn(BigDecimal.ZERO);
        when(referralWithdrawalRepository.sumAmountByAccountAndStatus(CUSTOMER_ID, ReferralWithdrawalStatus.PAID))
                .thenReturn(BigDecimal.ZERO);

        assertThatThrownBy(() -> referralService.requestWithdrawal(new ReferralWithdrawRequestDto(new BigDecimal("500"))))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode().value()).isEqualTo(400));
    }

    @Test
    void getMyReferralDashboard_forbiddenForAdmin() {
        AccountEntity admin = new AccountEntity();
        admin.setId(CUSTOMER_ID);
        admin.setRole(AccountRole.ADMIN);
        when(securityContextHelper.getCurrentAccountIdOrThrow()).thenReturn(CUSTOMER_ID);
        when(accountRepository.findById(CUSTOMER_ID)).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> referralService.getMyReferralDashboard())
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode().value()).isEqualTo(403));
    }

    @Test
    void listClicksForAdmin_forbiddenForCustomer() {
        when(securityContextHelper.requireAdminOrOwner(any()))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "x"));

        assertThatThrownBy(() -> referralService.listClicksForAdmin(PageRequest.of(0, 20)))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode().value()).isEqualTo(403));
    }

    @Test
    void getMyReferralDashboard_returnsStats() {
        AccountEntity me = customerWithCode(CUSTOMER_ID, "GG1-HH22");
        me.setRole(AccountRole.CUSTOMER);
        me.setReferredBy(null);

        when(securityContextHelper.getCurrentAccountIdOrThrow()).thenReturn(CUSTOMER_ID);
        when(accountRepository.findById(CUSTOMER_ID)).thenReturn(Optional.of(me));
        when(referralClickRepository.countByReferrerAccount_Id(CUSTOMER_ID)).thenReturn(3L);
        when(referralCommissionRepository.countByReferrerAccount_Id(CUSTOMER_ID)).thenReturn(2L);
        when(referralCommissionRepository.sumCommissionByReferrer(CUSTOMER_ID)).thenReturn(new BigDecimal("150.00"));
        when(referralWithdrawalRepository.sumAmountByAccountAndStatus(CUSTOMER_ID, ReferralWithdrawalStatus.PENDING))
                .thenReturn(BigDecimal.ZERO);
        when(referralWithdrawalRepository.sumAmountByAccountAndStatus(CUSTOMER_ID, ReferralWithdrawalStatus.PAID))
                .thenReturn(new BigDecimal("50.00"));

        ReferralMeResponseDto dto = referralService.getMyReferralDashboard();

        assertThat(dto.referralCode()).isEqualTo("GG1-HH22");
        assertThat(dto.referralLink()).isEqualTo("https://site.ru/ref/GG1-HH22");
        assertThat(dto.clickCount()).isEqualTo(3);
        assertThat(dto.purchaseCount()).isEqualTo(2);
        assertThat(dto.availableBalance()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(dto.inviteCodeApplied()).isFalse();
    }

    private static AccountEntity customerWithCode(UUID id, String code) {
        AccountEntity e = new AccountEntity();
        e.setId(id);
        e.setRole(AccountRole.CUSTOMER);
        e.setRegistrationStatus(AccountRegistrationStatus.ACTIVE);
        e.setIsBlocked(false);
        e.setReferralCode(code);
        return e;
    }
}
