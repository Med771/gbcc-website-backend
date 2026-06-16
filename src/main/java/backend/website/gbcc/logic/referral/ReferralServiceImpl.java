package backend.website.gbcc.logic.referral;

import backend.website.gbcc.config.property.ReferralProperty;
import backend.website.gbcc.helper.SecurityContextHelper;
import backend.website.gbcc.logic.account.AccountEntity;
import backend.website.gbcc.logic.account.AccountRepository;
import backend.website.gbcc.logic.order.OrderEntity;
import backend.website.gbcc.logic.order.OrderRepository;
import backend.website.gbcc.logic.referral.dto.ReferralApplyInviteCodeRequestDto;
import backend.website.gbcc.logic.referral.dto.ReferralClickAdminDto;
import backend.website.gbcc.logic.referral.dto.ReferralClickRequestDto;
import backend.website.gbcc.logic.referral.dto.ReferralCommissionAdminDto;
import backend.website.gbcc.logic.referral.dto.ReferralMeResponseDto;
import backend.website.gbcc.logic.referral.dto.ReferralRefereeRowDto;
import backend.website.gbcc.logic.referral.dto.ReferralRegistrationAdminDto;
import backend.website.gbcc.logic.referral.dto.ReferralUpdateMyCodeRequestDto;
import backend.website.gbcc.logic.referral.dto.ReferralWithdrawRequestDto;
import backend.website.gbcc.logic.referral.dto.ReferralWithdrawalAdminDto;
import backend.website.gbcc.logic.referral.dto.ReferralWithdrawalCreatedResponseDto;
import backend.website.gbcc.logic.referral.dto.ReferralWithdrawalRejectRequestDto;
import backend.website.gbcc.model.AccountRegistrationStatus;
import backend.website.gbcc.model.AccountRole;
import backend.website.gbcc.model.OrderStatus;
import backend.website.gbcc.model.ReferralClientType;
import backend.website.gbcc.model.ReferralWithdrawalStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReferralServiceImpl implements ReferralService {

    private static final String ADMIN_FORBIDDEN = "Only admin or owner can access referral administration";
    private static final String ONLY_CUSTOMER = "Referral program is only for customers";
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private final AccountRepository accountRepository;
    private final OrderRepository orderRepository;
    private final ReferralClickRepository referralClickRepository;
    private final ReferralCommissionRepository referralCommissionRepository;
    private final ReferralWithdrawalRepository referralWithdrawalRepository;
    private final ReferralProperty referralProperty;
    private final ReferralCodeGenerator referralCodeGenerator;
    private final SecurityContextHelper securityContextHelper;

    @Override
    @Transactional
    public void assignReferralCodeIfMissing(UUID customerAccountId) {
        AccountEntity acc = accountRepository.findById(customerAccountId).orElse(null);
        if (acc == null || acc.getRole() != AccountRole.CUSTOMER) {
            return;
        }
        ensureReferralCodeAssigned(acc);
    }

    @Override
    @Transactional
    public void bindInviterForNewCustomer(AccountEntity newCustomer, String rawInviteCode) {
        if (!StringUtils.hasText(rawInviteCode)) {
            return;
        }
        String canonical = ReferralCodeNormalizer.normalize(rawInviteCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid invitation code"));
        AccountEntity inviter = accountRepository.findByReferralCode(canonical)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid invitation code"));
        validateInviterAccount(inviter);
        newCustomer.setReferredBy(inviter);
    }

    @Override
    @Transactional
    public void recordClick(ReferralClickRequestDto requestDto) {
        String canonical = ReferralCodeNormalizer.normalize(requestDto.code())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid referral code"));
        AccountEntity referrer = accountRepository.findByReferralCode(canonical)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Referral code not found"));
        if (referrer.getRole() != AccountRole.CUSTOMER) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Referral code not found");
        }
        if (Boolean.TRUE.equals(referrer.getIsBlocked())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Referral code not found");
        }

        ReferralClickEntity click = new ReferralClickEntity();
        click.setReferrerAccount(referrer);
        click.setVisitorFingerprint(truncateFingerprint(requestDto.visitorFingerprint()));
        referralClickRepository.save(click);
    }

    @Override
    @Transactional
    public ReferralMeResponseDto getMyReferralDashboard() {
        AccountEntity customer = getCurrentCustomerOrThrow();
        ensureReferralCodeAssigned(customer);

        long clicks = referralClickRepository.countByReferrerAccount_Id(customer.getId());
        long purchases = referralCommissionRepository.countByReferrerAccount_Id(customer.getId());
        BigDecimal available = computeAvailableBalance(customer.getId());

        String code = customer.getReferralCode();
        String display = "#" + code;
        String link = buildReferralLink(code);

        AccountEntity referredBy = customer.getReferredBy();
        boolean inviteApplied = referredBy != null;
        String appliedDisplay = inviteApplied && referredBy.getReferralCode() != null
                ? "#" + referredBy.getReferralCode()
                : null;

        return new ReferralMeResponseDto(
                code,
                display,
                link,
                clicks,
                purchases,
                available,
                referralProperty.getMinWithdrawalAmount(),
                referralProperty.getCommissionPercentNewClient(),
                referralProperty.getCommissionPercentReturningClient(),
                inviteApplied,
                appliedDisplay
        );
    }

    @Override
    @Transactional
    public Page<ReferralRefereeRowDto> getMyReferees(Pageable pageable) {
        AccountEntity customer = getCurrentCustomerOrThrow();
        ensureReferralCodeAssigned(customer);

        return accountRepository.findByReferredBy_IdOrderByCreatedAtDesc(customer.getId(), pageable)
                .map(referee -> {
                    BigDecimal total = referralCommissionRepository.sumCommissionByReferrerAndReferee(
                            customer.getId(),
                            referee.getId()
                    );
                    long purchaseCount = referralCommissionRepository.countByReferrerAccount_IdAndRefereeAccount_Id(
                            customer.getId(),
                            referee.getId()
                    );
                    return new ReferralRefereeRowDto(
                            referee.getId(),
                            maskEmail(referee.getEmail()),
                            referee.getCreatedAt(),
                            total,
                            purchaseCount
                    );
                });
    }

    @Override
    @Transactional
    public void applyInviteCode(ReferralApplyInviteCodeRequestDto requestDto) {
        AccountEntity customer = getCurrentCustomerOrThrow();
        if (customer.getReferredBy() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Invitation code already applied");
        }
        String canonical = ReferralCodeNormalizer.normalize(requestDto.code())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid invitation code"));
        AccountEntity inviter = accountRepository.findByReferralCode(canonical)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid invitation code"));
        validateInviterAccount(inviter);
        if (inviter.getId().equals(customer.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot use own referral code");
        }
        customer.setReferredBy(inviter);
        accountRepository.save(customer);
    }

    @Override
    @Transactional
    public void updateMyReferralCode(ReferralUpdateMyCodeRequestDto requestDto) {
        AccountEntity customer = getCurrentCustomerOrThrow();
        ensureReferralCodeAssigned(customer);
        String canonical = ReferralCodeNormalizer.normalize(requestDto.code())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid referral code format"));
        if (canonical.equals(customer.getReferralCode())) {
            return;
        }
        if (accountRepository.findByReferralCode(canonical).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This referral code is already taken");
        }
        customer.setReferralCode(canonical);
        try {
            accountRepository.save(customer);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This referral code is already taken");
        }
    }

    @Override
    @Transactional
    public ReferralWithdrawalCreatedResponseDto requestWithdrawal(ReferralWithdrawRequestDto requestDto) {
        AccountEntity customer = getCurrentCustomerOrThrow();
        UUID accountId = customer.getId();
        BigDecimal available = computeAvailableBalance(accountId);
        BigDecimal min = referralProperty.getMinWithdrawalAmount();
        BigDecimal amount = requestDto.amount().setScale(2, RoundingMode.HALF_UP);

        if (amount.compareTo(min) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount is below minimum withdrawal");
        }
        if (amount.compareTo(available) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient available balance");
        }

        ReferralWithdrawalEntity row = new ReferralWithdrawalEntity();
        row.setAccount(customer);
        row.setAmount(amount);
        row.setStatus(ReferralWithdrawalStatus.PENDING);
        ReferralWithdrawalEntity saved = referralWithdrawalRepository.save(row);
        return new ReferralWithdrawalCreatedResponseDto(
                saved.getId(),
                saved.getAmount(),
                saved.getStatus(),
                saved.getCreatedAt()
        );
    }

    @Override
    @Transactional
    public void onOrderDelivered(UUID orderId) {
        if (referralCommissionRepository.existsByOrder_Id(orderId)) {
            return;
        }
        OrderEntity order = orderRepository.findByIdWithCustomerAndReferrer(orderId)
                .orElse(null);
        if (order == null) {
            return;
        }
        AccountEntity buyer = order.getCustomer();
        if (buyer.getRole() != AccountRole.CUSTOMER) {
            return;
        }
        AccountEntity referrer = buyer.getReferredBy();
        if (referrer == null) {
            return;
        }
        if (Boolean.TRUE.equals(referrer.getIsBlocked())) {
            return;
        }
        if (referrer.getRole() != AccountRole.CUSTOMER) {
            return;
        }
        if (referrer.getId().equals(buyer.getId())) {
            return;
        }

        BigDecimal base = order.getTotalDiscountedPrice();
        long priorDelivered = orderRepository.countByCustomer_IdAndStatus(buyer.getId(), OrderStatus.DELIVERED);
        ReferralClientType clientType = priorDelivered <= 1 ? ReferralClientType.NEW : ReferralClientType.RETURNING;
        BigDecimal pct = clientType == ReferralClientType.NEW
                ? referralProperty.getCommissionPercentNewClient()
                : referralProperty.getCommissionPercentReturningClient();
        BigDecimal commission = base.multiply(pct).divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
        if (commission.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        ReferralCommissionEntity row = new ReferralCommissionEntity();
        row.setReferrerAccount(referrer);
        row.setRefereeAccount(buyer);
        row.setOrder(order);
        row.setOrderAmount(base);
        row.setCommissionPercent(pct);
        row.setCommissionAmount(commission);
        row.setClientType(clientType);
        referralCommissionRepository.save(row);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReferralClickAdminDto> listClicksForAdmin(Pageable pageable) {
        securityContextHelper.requireAdminOrOwner(ADMIN_FORBIDDEN);
        return referralClickRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(c -> new ReferralClickAdminDto(
                        c.getId(),
                        c.getCreatedAt(),
                        c.getReferrerAccount().getId(),
                        c.getReferrerAccount().getEmail(),
                        c.getReferrerAccount().getReferralCode(),
                        c.getVisitorFingerprint()
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReferralCommissionAdminDto> listCommissionsForAdmin(Pageable pageable) {
        securityContextHelper.requireAdminOrOwner(ADMIN_FORBIDDEN);
        return referralCommissionRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(c -> new ReferralCommissionAdminDto(
                        c.getId(),
                        c.getCreatedAt(),
                        c.getOrder().getId(),
                        c.getOrder().getDisplayNumber(),
                        c.getReferrerAccount().getId(),
                        c.getReferrerAccount().getEmail(),
                        c.getRefereeAccount().getId(),
                        c.getRefereeAccount().getEmail(),
                        c.getOrderAmount(),
                        c.getCommissionPercent(),
                        c.getCommissionAmount(),
                        c.getClientType()
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReferralWithdrawalAdminDto> listWithdrawalsForAdmin(Pageable pageable) {
        securityContextHelper.requireAdminOrOwner(ADMIN_FORBIDDEN);
        return referralWithdrawalRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(w -> new ReferralWithdrawalAdminDto(
                        w.getId(),
                        w.getCreatedAt(),
                        w.getAccount().getId(),
                        w.getAccount().getEmail(),
                        w.getAmount(),
                        w.getStatus(),
                        w.getProcessedAt(),
                        w.getProcessedByAccount() != null ? w.getProcessedByAccount().getId() : null,
                        w.getProcessedByAccount() != null ? w.getProcessedByAccount().getEmail() : null,
                        w.getAdminNote()
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReferralRegistrationAdminDto> listRegistrationsWithInviterForAdmin(Pageable pageable) {
        securityContextHelper.requireAdminOrOwner(ADMIN_FORBIDDEN);
        return accountRepository.findByReferredByIsNotNullOrderByCreatedAtDesc(pageable)
                .map(referee -> {
                    AccountEntity inviter = referee.getReferredBy();
                    return new ReferralRegistrationAdminDto(
                            referee.getId(),
                            referee.getEmail(),
                            referee.getCreatedAt(),
                            inviter.getId(),
                            inviter.getEmail(),
                            inviter.getReferralCode()
                    );
                });
    }

    @Override
    @Transactional
    public void markWithdrawalPaid(UUID withdrawalId) {
        securityContextHelper.requireAdminOrOwner(ADMIN_FORBIDDEN);
        UUID adminId = securityContextHelper.getCurrentAccountIdOrThrow();
        AccountEntity admin = accountRepository.findById(adminId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account not found"));

        ReferralWithdrawalEntity w = referralWithdrawalRepository.findById(withdrawalId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Withdrawal not found"));
        if (w.getStatus() != ReferralWithdrawalStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Withdrawal is not pending");
        }
        w.setStatus(ReferralWithdrawalStatus.PAID);
        w.setProcessedAt(java.time.Instant.now());
        w.setProcessedByAccount(admin);
        referralWithdrawalRepository.save(w);
    }

    @Override
    @Transactional
    public void rejectWithdrawal(UUID withdrawalId, ReferralWithdrawalRejectRequestDto requestDto) {
        securityContextHelper.requireAdminOrOwner(ADMIN_FORBIDDEN);
        UUID adminId = securityContextHelper.getCurrentAccountIdOrThrow();
        AccountEntity admin = accountRepository.findById(adminId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account not found"));

        ReferralWithdrawalEntity w = referralWithdrawalRepository.findById(withdrawalId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Withdrawal not found"));
        if (w.getStatus() != ReferralWithdrawalStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Withdrawal is not pending");
        }
        w.setStatus(ReferralWithdrawalStatus.REJECTED);
        w.setProcessedAt(java.time.Instant.now());
        w.setProcessedByAccount(admin);
        if (requestDto != null && StringUtils.hasText(requestDto.adminNote())) {
            w.setAdminNote(requestDto.adminNote().trim());
        }
        referralWithdrawalRepository.save(w);
    }

    private void ensureReferralCodeAssigned(AccountEntity customer) {
        if (customer.getReferralCode() != null) {
            return;
        }
        String code = referralCodeGenerator.generateUniqueCode(
                candidate -> accountRepository.findByReferralCode(candidate).isPresent()
        );
        customer.setReferralCode(code);
        try {
            accountRepository.save(customer);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Could not assign referral code, try again");
        }
    }

    private AccountEntity getCurrentCustomerOrThrow() {
        UUID id = securityContextHelper.getCurrentAccountIdOrThrow();
        AccountEntity acc = accountRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account not found"));
        if (acc.getRole() != AccountRole.CUSTOMER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, ONLY_CUSTOMER);
        }
        return acc;
    }

    private void validateInviterAccount(AccountEntity inviter) {
        if (inviter.getRole() != AccountRole.CUSTOMER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid invitation code");
        }
        if (inviter.getRegistrationStatus() != AccountRegistrationStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid invitation code");
        }
        if (Boolean.TRUE.equals(inviter.getIsBlocked())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid invitation code");
        }
        if (!StringUtils.hasText(inviter.getReferralCode())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid invitation code");
        }
    }

    private BigDecimal computeAvailableBalance(UUID referrerAccountId) {
        BigDecimal earned = referralCommissionRepository.sumCommissionByReferrer(referrerAccountId);
        BigDecimal pending = referralWithdrawalRepository.sumAmountByAccountAndStatus(
                referrerAccountId,
                ReferralWithdrawalStatus.PENDING
        );
        BigDecimal paid = referralWithdrawalRepository.sumAmountByAccountAndStatus(
                referrerAccountId,
                ReferralWithdrawalStatus.PAID
        );
        return earned.subtract(paid).subtract(pending).setScale(2, RoundingMode.HALF_UP);
    }

    private String buildReferralLink(String code) {
        String base = referralProperty.getLinkBaseUrl().trim();
        while (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base + "/" + code;
    }

    private static String maskEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return "***";
        }
        int at = email.indexOf('@');
        if (at <= 0) {
            return "***";
        }
        return email.charAt(0) + "***" + email.substring(at);
    }

    private static String truncateFingerprint(String fingerprint) {
        if (!StringUtils.hasText(fingerprint)) {
            return null;
        }
        String t = fingerprint.trim();
        return t.length() > 128 ? t.substring(0, 128) : t;
    }
}
