package backend.website.gbcc.logic.crm.contract;

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

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "crm_contract_line")
@Getter
@Setter
@NoArgsConstructor
public class CrmContractLineEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contract_id", nullable = false)
    private CrmContractEntity contract;

    @Column(name = "planned_date")
    private LocalDate plannedDate;

    @Column(name = "planned_quantity", precision = 18, scale = 3)
    private BigDecimal plannedQuantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "line_status", nullable = false, length = 32)
    private CrmContractLineStatus lineStatus = CrmContractLineStatus.PENDING;

    @Column(name = "comment_text", columnDefinition = "text")
    private String commentText;
}
