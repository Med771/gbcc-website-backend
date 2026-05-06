package backend.website.gbcc.logic.crm.interaction;

import backend.website.gbcc.logic.account.AccountEntity;
import backend.website.gbcc.logic.crm.lead.CrmLeadEntity;
import backend.website.gbcc.logic.crm.organization.CrmOrganizationEntity;
import backend.website.gbcc.logic.crm.task.CrmTaskEntity;
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
@Table(name = "crm_interaction")
@Getter
@Setter
@NoArgsConstructor
public class CrmInteractionEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private CrmOrganizationEntity organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_id")
    private CrmLeadEntity lead;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_account_id", nullable = false)
    private AccountEntity author;

    @Column(name = "result_note", length = 512)
    private String resultNote;

    @Column(name = "comment_text", columnDefinition = "text")
    private String commentText;

    @Column(name = "next_step", columnDefinition = "text")
    private String nextStep;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "next_task_id")
    private CrmTaskEntity nextTask;
}
