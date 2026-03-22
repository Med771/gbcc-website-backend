package backend.website.gbcc.logic.cooperationrequest;

import backend.website.gbcc.logic.account.AccountEntity;
import backend.website.gbcc.logic.file.FileEntity;
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
@Table(name = "cooperation_request")
@Getter
@Setter
@NoArgsConstructor
public class CooperationRequestEntity extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 64)
    private String phone;

    @Column(nullable = false)
    private String email;

    @Column(name = "cooperation_type", length = 128)
    private String cooperationType;

    @Column(columnDefinition = "text")
    private String comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attachment_file_id")
    private FileEntity attachmentFile;

    @Column(name = "consent_processing", nullable = false)
    private Boolean consentProcessing;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_account_id")
    private AccountEntity assignedTo;

    @Column(name = "assigned_at")
    private Instant assignedAt;
}
