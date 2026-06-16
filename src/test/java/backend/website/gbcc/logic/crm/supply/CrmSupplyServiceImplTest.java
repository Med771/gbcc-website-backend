package backend.website.gbcc.logic.crm.supply;

import backend.website.gbcc.logic.crm.access.CrmAccessPolicy;
import backend.website.gbcc.logic.crm.contract.CrmContractLineRepository;
import backend.website.gbcc.logic.crm.organization.CrmOrganizationEntity;
import backend.website.gbcc.logic.crm.organization.CrmOrganizationBranchRepository;
import backend.website.gbcc.logic.crm.organization.CrmOrganizationRepository;
import backend.website.gbcc.logic.crm.supply.dto.CreateCrmSupplyRequestDto;
import backend.website.gbcc.logic.product.ProductRepository;
import backend.website.gbcc.logic.crm.supply.dto.UpdateCrmSupplyRequestDto;
import backend.website.gbcc.model.AccountPrincipal;
import backend.website.gbcc.model.AccountRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CrmSupplyServiceImplTest {

    private static final UUID OWNER_ID = UUID.fromString("00000000-0000-4000-8000-000000000001");
    private static final UUID ORG_ID = UUID.fromString("00000000-0000-4000-8000-000000000002");
    private static final UUID SUPPLY_ID = UUID.fromString("00000000-0000-4000-8000-000000000003");

    @Mock
    private CrmSupplyRepository supplyRepository;

    @Mock
    private CrmOrganizationRepository organizationRepository;

    @Mock
    private CrmOrganizationBranchRepository branchRepository;

    @Mock
    private CrmContractLineRepository contractLineRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CrmAccessPolicy crmAccessPolicy;

    @InjectMocks
    private CrmSupplyServiceImpl supplyService;

    @Test
    void create_shouldPersistViaRepository_andTrimDescription() {
        when(crmAccessPolicy.requireCrmUser()).thenReturn(new AccountPrincipal(OWNER_ID, AccountRole.OWNER));

        CrmOrganizationEntity org = new CrmOrganizationEntity();
        org.setId(ORG_ID);
        org.setName("Org");
        org.setAssignedTo(null);
        when(organizationRepository.findById(ORG_ID)).thenReturn(Optional.of(org));

        when(supplyRepository.save(any(CrmSupplyEntity.class))).thenAnswer(inv -> {
            CrmSupplyEntity e = inv.getArgument(0);
            e.setId(SUPPLY_ID);
            e.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
            e.setUpdatedAt(Instant.parse("2026-01-01T00:00:00Z"));
            return e;
        });

        CreateCrmSupplyRequestDto dto = new CreateCrmSupplyRequestDto(
                ORG_ID,
                LocalDate.of(2026, 2, 1),
                null,
                "  pallets  ",
                new BigDecimal("12.5"),
                CrmSupplyStatus.PLANNED,
                "  note  ",
                null,
                null,
                55.75,
                37.62,
                null,
                null,
                null
        );

        var res = supplyService.create(dto);

        assertThat(res.id()).isEqualTo(SUPPLY_ID);
        assertThat(res.productDescription()).isEqualTo("pallets");
        assertThat(res.commentText()).isEqualTo("note");

        ArgumentCaptor<CrmSupplyEntity> captor = ArgumentCaptor.forClass(CrmSupplyEntity.class);
        verify(supplyRepository).save(captor.capture());
        CrmSupplyEntity saved = captor.getValue();
        assertThat(saved.getOrganization()).isSameAs(org);
        assertThat(saved.getSupplyAt()).isEqualTo(LocalDate.of(2026, 2, 1));
        assertThat(saved.getQuantity()).isEqualByComparingTo(new BigDecimal("12.5"));
        assertThat(saved.getStatus()).isEqualTo(CrmSupplyStatus.PLANNED);
        assertThat(saved.getDeliveryLatitude()).isEqualTo(55.75);
        assertThat(saved.getDeliveryLongitude()).isEqualTo(37.62);
        assertThat(saved.getContractLine()).isNull();

        verify(crmAccessPolicy).assertCanModifyOrganization(null);
    }

    @Test
    void update_shouldSaveMutatedEntity() {
        when(crmAccessPolicy.requireCrmUser()).thenReturn(new AccountPrincipal(OWNER_ID, AccountRole.OWNER));

        CrmOrganizationEntity org = new CrmOrganizationEntity();
        org.setId(ORG_ID);
        org.setAssignedTo(null);

        CrmSupplyEntity existing = new CrmSupplyEntity();
        existing.setId(SUPPLY_ID);
        existing.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        existing.setUpdatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        existing.setOrganization(org);
        existing.setSupplyAt(LocalDate.of(2026, 1, 1));
        existing.setProductDescription("old");
        existing.setQuantity(BigDecimal.ONE);
        existing.setStatus(CrmSupplyStatus.PLANNED);
        when(supplyRepository.findById(SUPPLY_ID)).thenReturn(Optional.of(existing));

        when(supplyRepository.save(existing)).thenReturn(existing);

        UpdateCrmSupplyRequestDto dto = new UpdateCrmSupplyRequestDto(
                LocalDate.of(2026, 3, 3),
                null,
                " new desc ",
                new BigDecimal("2"),
                CrmSupplyStatus.IN_TRANSIT,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        var res = supplyService.update(SUPPLY_ID, dto);

        assertThat(res.status()).isEqualTo(CrmSupplyStatus.IN_TRANSIT);
        assertThat(res.productDescription()).isEqualTo("new desc");
        assertThat(existing.getProductDescription()).isEqualTo("new desc");
        verify(supplyRepository).save(existing);
    }

    @Test
    void delete_shouldCallRepositoryDelete() {
        when(crmAccessPolicy.requireCrmUser()).thenReturn(new AccountPrincipal(OWNER_ID, AccountRole.OWNER));

        CrmOrganizationEntity org = new CrmOrganizationEntity();
        org.setId(ORG_ID);
        org.setAssignedTo(null);

        CrmSupplyEntity existing = new CrmSupplyEntity();
        existing.setId(SUPPLY_ID);
        existing.setOrganization(org);
        when(supplyRepository.findById(SUPPLY_ID)).thenReturn(Optional.of(existing));

        supplyService.delete(SUPPLY_ID);

        verify(supplyRepository).delete(existing);
    }
}
