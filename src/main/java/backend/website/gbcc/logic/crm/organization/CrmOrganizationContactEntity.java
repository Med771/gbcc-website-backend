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
@Table(name = "crm_organization_contact")
@Getter
@Setter
@NoArgsConstructor
public class CrmOrganizationContactEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private CrmOrganizationEntity organization;

    @Column(name = "full_name")
    private String fullName;

    @Column(length = 255)
    private String department;

    @Column(length = 64)
    private String phone;

    @Column(length = 255)
    private String email;

    @Column(name = "extra_note", columnDefinition = "text")
    private String extraNote;
}
