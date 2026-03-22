package backend.website.gbcc.logic.contactrequest;

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

import java.time.Instant;

@Entity
@Table(name = "contact_request")
@Getter
@Setter
@NoArgsConstructor
public class ContactRequestEntity extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(length = 64)
    private String phone;

    @Column(nullable = false, columnDefinition = "text")
    private String message;

    @Column(name = "consent_processing", nullable = false)
    private Boolean consentProcessing;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_account_id")
    private AccountEntity assignedTo;

    @Column(name = "assigned_at")
    private Instant assignedAt;
}
