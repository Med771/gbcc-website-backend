package backend.website.gbcc.logic.crm.contract;

import backend.website.gbcc.logic.crm.organization.CrmOrganizationEntity;
import backend.website.gbcc.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "crm_contract")
@Getter
@Setter
@NoArgsConstructor
public class CrmContractEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private CrmOrganizationEntity organization;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "comment_text", columnDefinition = "text")
    private String commentText;

    @OneToMany(mappedBy = "contract", fetch = FetchType.LAZY)
    private List<CrmContractLineEntity> lines = new ArrayList<>();
}
