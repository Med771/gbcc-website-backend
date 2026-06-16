package backend.website.gbcc.logic.crm.organization;

import backend.website.gbcc.logic.account.AccountEntity;
import backend.website.gbcc.logic.contactrequest.ContactRequestEntity;
import backend.website.gbcc.logic.cooperationrequest.CooperationRequestEntity;
import backend.website.gbcc.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "crm_organization")
@Getter
@Setter
@NoArgsConstructor
public class CrmOrganizationEntity extends BaseEntity {

    @Column(nullable = false, length = 512)
    private String name;

    @Column(name = "external_number", length = 128)
    private String externalNumber;

    @Column(name = "legal_address", columnDefinition = "text")
    private String legalAddress;

    @Column(name = "delivery_address", columnDefinition = "text")
    private String deliveryAddress;

    @Column(length = 12)
    private String inn;

    @Column(name = "floor_note", length = 255)
    private String floorNote;

    @Column(name = "comment_general", columnDefinition = "text")
    private String commentGeneral;

    @Column(name = "product_types_note", columnDefinition = "text")
    private String productTypesNote;

    @Column(name = "supply_volume_note", columnDefinition = "text")
    private String supplyVolumeNote;

    @Column(name = "supply_schedule_note", columnDefinition = "text")
    private String supplyScheduleNote;

    @Column(name = "cooperation_until")
    private LocalDate cooperationUntil;

    @Enumerated(EnumType.STRING)
    @Column(name = "client_status", nullable = false, length = 32)
    private CrmClientStatus clientStatus = CrmClientStatus.PROSPECT;

    private Double latitude;

    private Double longitude;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_account_id")
    private AccountEntity assignedTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "converted_from_contact_request_id")
    private ContactRequestEntity convertedFromContactRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "converted_from_cooperation_request_id")
    private CooperationRequestEntity convertedFromCooperationRequest;

    @OneToMany(mappedBy = "organization", fetch = FetchType.LAZY)
    private List<CrmOrganizationContactEntity> contacts = new ArrayList<>();
}
