package backend.website.gbcc.logic.crm.supply;

import backend.website.gbcc.logic.crm.contract.CrmContractLineEntity;
import backend.website.gbcc.logic.crm.organization.CrmOrganizationBranchEntity;
import backend.website.gbcc.logic.crm.organization.CrmOrganizationEntity;
import backend.website.gbcc.logic.product.ProductEntity;
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
import java.time.LocalTime;

@Entity
@Table(name = "crm_supply")
@Getter
@Setter
@NoArgsConstructor
public class CrmSupplyEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private CrmOrganizationEntity organization;

    @Column(name = "supply_at", nullable = false)
    private LocalDate supplyAt;

    @Column(name = "supply_time")
    private LocalTime supplyTime;

    @Column(name = "product_description", nullable = false, columnDefinition = "text")
    private String productDescription;

    private BigDecimal quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private CrmSupplyStatus status = CrmSupplyStatus.PLANNED;

    @Column(name = "comment_text", columnDefinition = "text")
    private String commentText;

    @Column(name = "delivery_address", columnDefinition = "text")
    private String deliveryAddress;

    @Column(name = "object_name", length = 512)
    private String objectName;

    @Column(name = "delivery_latitude")
    private Double deliveryLatitude;

    @Column(name = "delivery_longitude")
    private Double deliveryLongitude;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private CrmOrganizationBranchEntity branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private ProductEntity product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_line_id")
    private CrmContractLineEntity contractLine;
}
