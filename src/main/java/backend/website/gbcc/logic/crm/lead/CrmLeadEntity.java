package backend.website.gbcc.logic.crm.lead;

import backend.website.gbcc.logic.account.AccountEntity;
import backend.website.gbcc.logic.crm.organization.CrmOrganizationEntity;
import backend.website.gbcc.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "crm_lead")
@Getter
@Setter
@NoArgsConstructor
public class CrmLeadEntity extends BaseEntity {

    @Column(name = "company_name", nullable = false, length = 512)
    private String companyName;

    @Column(columnDefinition = "text")
    private String phones;

    @Column(name = "presumed_contacts", columnDefinition = "text")
    private String presumedContacts;

    @Column(name = "manager_comment", columnDefinition = "text")
    private String managerComment;

    @Column(length = 255)
    private String department;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private CrmLeadStatus status = CrmLeadStatus.NEW;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_account_id")
    private AccountEntity assignedTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "converted_organization_id")
    private CrmOrganizationEntity convertedOrganization;
}
