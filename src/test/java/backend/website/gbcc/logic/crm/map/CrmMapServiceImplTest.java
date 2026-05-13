package backend.website.gbcc.logic.crm.map;

import backend.website.gbcc.logic.crm.access.CrmAccessPolicy;
import backend.website.gbcc.logic.crm.map.dto.CreateCrmCompanyObjectRequestDto;
import backend.website.gbcc.logic.crm.map.dto.UpdateCrmCompanyObjectRequestDto;
import backend.website.gbcc.logic.crm.organization.CrmOrganizationRepository;
import backend.website.gbcc.logic.crm.supply.CrmSupplyRepository;
import backend.website.gbcc.model.AccountPrincipal;
import backend.website.gbcc.model.AccountRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CrmMapServiceImplTest {

    private static final UUID OWNER_ID = UUID.fromString("00000000-0000-4000-8000-000000000031");
    private static final UUID OBJ_ID = UUID.fromString("00000000-0000-4000-8000-000000000032");

    @Mock
    private CrmOrganizationRepository organizationRepository;

    @Mock
    private CrmSupplyRepository supplyRepository;

    @Mock
    private CrmCompanyObjectRepository companyObjectRepository;

    @Mock
    private CrmAccessPolicy crmAccessPolicy;

    @InjectMocks
    private CrmMapServiceImpl mapService;

    @Test
    void createCompanyObject_shouldPersistViaRepository() {
        when(crmAccessPolicy.requireCrmUser()).thenReturn(new AccountPrincipal(OWNER_ID, AccountRole.OWNER));

        when(companyObjectRepository.save(any(CrmCompanyObjectEntity.class))).thenAnswer(inv -> {
            CrmCompanyObjectEntity e = inv.getArgument(0);
            e.setId(OBJ_ID);
            e.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
            e.setUpdatedAt(Instant.parse("2026-01-01T00:00:00Z"));
            return e;
        });

        var res = mapService.createCompanyObject(new CreateCrmCompanyObjectRequestDto(
                "  Warehouse  ",
                59.93,
                30.31,
                "  near port  "
        ));

        assertThat(res.id()).isEqualTo(OBJ_ID);
        assertThat(res.name()).isEqualTo("Warehouse");
        assertThat(res.commentText()).isEqualTo("near port");

        ArgumentCaptor<CrmCompanyObjectEntity> captor = ArgumentCaptor.forClass(CrmCompanyObjectEntity.class);
        verify(companyObjectRepository).save(captor.capture());
        CrmCompanyObjectEntity saved = captor.getValue();
        assertThat(saved.getLatitude()).isEqualTo(59.93);
        assertThat(saved.getLongitude()).isEqualTo(30.31);
    }

    @Test
    void updateCompanyObject_shouldSaveMutatedEntity() {
        when(crmAccessPolicy.requireCrmUser()).thenReturn(new AccountPrincipal(OWNER_ID, AccountRole.OWNER));

        CrmCompanyObjectEntity existing = new CrmCompanyObjectEntity();
        existing.setId(OBJ_ID);
        existing.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        existing.setUpdatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        existing.setName("old");
        existing.setLatitude(1);
        existing.setLongitude(2);
        when(companyObjectRepository.findById(OBJ_ID)).thenReturn(Optional.of(existing));
        when(companyObjectRepository.save(existing)).thenReturn(existing);

        mapService.updateCompanyObject(OBJ_ID, new UpdateCrmCompanyObjectRequestDto(
                " new ",
                10,
                20,
                null
        ));

        assertThat(existing.getName()).isEqualTo("new");
        assertThat(existing.getLatitude()).isEqualTo(10);
        assertThat(existing.getLongitude()).isEqualTo(20);
        verify(companyObjectRepository).save(existing);
    }

    @Test
    void deleteCompanyObject_shouldCallRepositoryDelete() {
        when(crmAccessPolicy.requireCrmUser()).thenReturn(new AccountPrincipal(OWNER_ID, AccountRole.OWNER));

        CrmCompanyObjectEntity existing = new CrmCompanyObjectEntity();
        existing.setId(OBJ_ID);
        when(companyObjectRepository.findById(OBJ_ID)).thenReturn(Optional.of(existing));

        mapService.deleteCompanyObject(OBJ_ID);

        verify(companyObjectRepository).delete(existing);
    }
}
