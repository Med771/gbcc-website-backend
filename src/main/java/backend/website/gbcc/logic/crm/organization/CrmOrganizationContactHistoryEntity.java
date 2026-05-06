package backend.website.gbcc.logic.crm.organization;

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
@Table(name = "crm_organization_contact_history")
@Getter
@Setter
@NoArgsConstructor
public class CrmOrganizationContactHistoryEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contact_id", nullable = false)
    private CrmOrganizationContactEntity contact;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "changed_by_account_id", nullable = false)
    private AccountEntity changedBy;

    @Column(name = "previous_snapshot", nullable = false, columnDefinition = "text")
    private String previousSnapshot;

    @Column(name = "new_snapshot", nullable = false, columnDefinition = "text")
    private String newSnapshot;
}
