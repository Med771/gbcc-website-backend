package backend.website.gbcc.logic.referral;

import backend.website.gbcc.logic.account.AccountEntity;
import backend.website.gbcc.model.BaseEntity;
import backend.website.gbcc.model.ReferralWithdrawalStatus;
import backend.website.gbcc.model.convector.ReferralWithdrawalStatusConverter;
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
import java.time.Instant;

@Entity
@Table(name = "referral_withdrawal")
@Getter
@Setter
@NoArgsConstructor
public class ReferralWithdrawalEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private AccountEntity account;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Convert(converter = ReferralWithdrawalStatusConverter.class)
    @Column(nullable = false, length = 32)
    private ReferralWithdrawalStatus status = ReferralWithdrawalStatus.PENDING;

    @Column(name = "processed_at")
    private Instant processedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by_account_id")
    private AccountEntity processedByAccount;

    @Column(name = "admin_note", length = 2000)
    private String adminNote;
}
