package backend.website.gbcc.logic.managercommission;

import backend.website.gbcc.logic.account.AccountEntity;
import backend.website.gbcc.logic.order.OrderEntity;
import backend.website.gbcc.model.BaseEntity;
import backend.website.gbcc.model.ReferralClientType;
import backend.website.gbcc.model.convector.ReferralClientTypeConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "manager_referral_commission")
@Getter
@Setter
@NoArgsConstructor
public class ManagerReferralCommissionEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "manager_account_id", nullable = false)
    private AccountEntity managerAccount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_account_id", nullable = false)
    private AccountEntity customerAccount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private OrderEntity order;

    @Column(name = "order_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal orderAmount;

    @Column(name = "commission_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal commissionPercent;

    @Column(name = "commission_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal commissionAmount;

    @Convert(converter = ReferralClientTypeConverter.class)
    @Column(name = "client_type", nullable = false, length = 16)
    private ReferralClientType clientType;
}
