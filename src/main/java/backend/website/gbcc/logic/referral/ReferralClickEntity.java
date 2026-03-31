package backend.website.gbcc.logic.referral;

import backend.website.gbcc.logic.account.AccountEntity;
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

@Entity
@Table(name = "referral_click")
@Getter
@Setter
@NoArgsConstructor
public class ReferralClickEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "referrer_account_id", nullable = false)
    private AccountEntity referrerAccount;

    @Column(name = "visitor_fingerprint", length = 128)
    private String visitorFingerprint;
}
