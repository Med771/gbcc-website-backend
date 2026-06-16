package backend.website.gbcc.logic.crm.organization;

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
@Table(name = "crm_organization_branch")
@Getter
@Setter
@NoArgsConstructor
public class CrmOrganizationBranchEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private CrmOrganizationEntity organization;

    @Column(nullable = false, length = 512)
    private String name;

    @Column(nullable = false, columnDefinition = "text")
    private String address;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;
}
