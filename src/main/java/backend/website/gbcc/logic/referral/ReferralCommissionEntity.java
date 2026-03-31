package backend.website.gbcc.logic.referral;

import backend.website.gbcc.logic.account.AccountEntity;
import backend.website.gbcc.logic.order.OrderEntity;
import backend.website.gbcc.model.BaseEntity;
import jakarta.persistence.Column;
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
@Table(name = "referral_commission")
@Getter
@Setter
@NoArgsConstructor
public class ReferralCommissionEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "referrer_account_id", nullable = false)
    private AccountEntity referrerAccount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "referee_account_id", nullable = false)
    private AccountEntity refereeAccount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private OrderEntity order;

    @Column(name = "order_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal orderAmount;

    @Column(name = "commission_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal commissionAmount;
}
