package backend.website.gbcc.logic.staff;

import backend.website.gbcc.logic.account.AccountEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "staff_profile")
@Getter
@Setter
@NoArgsConstructor
public class StaffProfileEntity {

    @Id
    @Column(name = "account_id")
    private UUID accountId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "account_id", nullable = false)
    private AccountEntity account;

    @Column(name = "position_title")
    private String positionTitle;

    @Column(name = "social_link", length = 512)
    private String socialLink;

    @Column(name = "bank_account_details", columnDefinition = "text")
    private String bankAccountDetails;

    @Column(name = "display_id", unique = true, length = 32)
    private String displayId;
}
