package backend.website.gbcc.logic.managercommission;

import backend.website.gbcc.config.property.ReferralProperty;
import backend.website.gbcc.helper.SecurityContextHelper;
import backend.website.gbcc.logic.account.AccountEntity;
import backend.website.gbcc.logic.managercommission.dto.ManagerCommissionAdminDto;
import backend.website.gbcc.logic.order.OrderEntity;
import backend.website.gbcc.logic.order.OrderRepository;
import backend.website.gbcc.model.AccountRole;
import backend.website.gbcc.model.OrderStatus;
import backend.website.gbcc.model.ReferralClientType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ManagerCommissionServiceImpl implements ManagerCommissionService {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private final ManagerReferralCommissionRepository managerReferralCommissionRepository;
    private final OrderRepository orderRepository;
    private final ReferralProperty referralProperty;
    private final SecurityContextHelper securityContextHelper;

    @Override
    @Transactional
    public void onOrderDelivered(UUID orderId) {
        if (managerReferralCommissionRepository.existsByOrder_Id(orderId)) {
            return;
        }
        OrderEntity order = orderRepository.findByIdWithCustomerAndReferrer(orderId).orElse(null);
        if (order == null) {
            return;
        }
        AccountEntity buyer = order.getCustomer();
        if (buyer.getRole() != AccountRole.CUSTOMER) {
            return;
        }
        AccountEntity manager = buyer.getBroughtByManager();
        if (manager == null) {
            return;
        }
        if (manager.getRole() != AccountRole.ADMIN && manager.getRole() != AccountRole.OWNER) {
            return;
        }
        if (Boolean.TRUE.equals(manager.getIsBlocked())) {
            return;
        }

        long priorDelivered = orderRepository.countByCustomer_IdAndStatus(buyer.getId(), OrderStatus.DELIVERED);
        ReferralClientType clientType = priorDelivered <= 1 ? ReferralClientType.NEW : ReferralClientType.RETURNING;
        BigDecimal pct = clientType == ReferralClientType.NEW
                ? referralProperty.getCommissionPercentNewClient()
                : referralProperty.getCommissionPercentReturningClient();

        BigDecimal base = order.getTotalDiscountedPrice();
        BigDecimal commission = base.multiply(pct).divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
        if (commission.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        ManagerReferralCommissionEntity row = new ManagerReferralCommissionEntity();
        row.setManagerAccount(manager);
        row.setCustomerAccount(buyer);
        row.setOrder(order);
        row.setOrderAmount(base);
        row.setCommissionPercent(pct);
        row.setCommissionAmount(commission);
        row.setClientType(clientType);
        managerReferralCommissionRepository.save(row);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ManagerCommissionAdminDto> listForAdmin(UUID managerId, Pageable pageable) {
        securityContextHelper.requireAdminOrOwner("Only admin or owner can view manager commissions");
        Page<ManagerReferralCommissionEntity> page = managerId != null
                ? managerReferralCommissionRepository.findAllByManagerAccount_IdOrderByCreatedAtDesc(managerId, pageable)
                : managerReferralCommissionRepository.findAllByOrderByCreatedAtDesc(pageable);
        return page.map(this::toDto);
    }

    private ManagerCommissionAdminDto toDto(ManagerReferralCommissionEntity entity) {
        return new ManagerCommissionAdminDto(
                entity.getId(),
                entity.getCreatedAt(),
                entity.getOrder().getId(),
                entity.getOrder().getDisplayNumber(),
                entity.getManagerAccount().getId(),
                entity.getManagerAccount().getEmail(),
                entity.getCustomerAccount().getId(),
                entity.getCustomerAccount().getEmail(),
                entity.getOrderAmount(),
                entity.getCommissionPercent(),
                entity.getCommissionAmount(),
                entity.getClientType()
        );
    }
}
