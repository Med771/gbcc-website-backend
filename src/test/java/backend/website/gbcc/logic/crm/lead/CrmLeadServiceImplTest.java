package backend.website.gbcc.logic.crm.lead;

import backend.website.gbcc.logic.account.AccountEntity;
import backend.website.gbcc.logic.account.AccountRepository;
import backend.website.gbcc.logic.crm.access.CrmAccessPolicy;
import backend.website.gbcc.logic.crm.lead.dto.CreateCrmLeadRequestDto;
import backend.website.gbcc.logic.crm.lead.dto.CrmLeadResponseDto;
import backend.website.gbcc.logic.crm.lead.dto.CrmReassignLeadRequestDto;
import backend.website.gbcc.logic.crm.lead.dto.UpdateCrmLeadRequestDto;
import backend.website.gbcc.logic.crm.organization.CrmClientStatus;
import backend.website.gbcc.logic.crm.organization.CrmOrganizationEntity;
import backend.website.gbcc.logic.crm.organization.CrmOrganizationRepository;
import backend.website.gbcc.logic.crm.organization.CrmOrganizationService;
import backend.website.gbcc.logic.crm.organization.dto.CrmOrganizationResponseDto;
import backend.website.gbcc.model.AccountPrincipal;
import backend.website.gbcc.model.AccountRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CrmLeadServiceImplTest {

    private static final UUID OWNER_ID = UUID.fromString("00000000-0000-4000-8000-000000000061");
    private static final UUID ADMIN_ID = UUID.fromString("00000000-0000-4000-8000-000000000062");
    private static final UUID LEAD_ID = UUID.fromString("00000000-0000-4000-8000-000000000063");
    private static final UUID NEW_ORG_ID = UUID.fromString("00000000-0000-4000-8000-000000000064");

    @Mock
    private CrmLeadRepository leadRepository;

    @Mock
    private CrmOrganizationRepository organizationRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CrmAccessPolicy crmAccessPolicy;

    @Mock
    private CrmOrganizationService organizationService;

    @InjectMocks
    private CrmLeadServiceImpl leadService;

    @Test
    void create_shouldPersistLead_forOwnerWithoutAssignee() {
        when(crmAccessPolicy.requireCrmUser()).thenReturn(new AccountPrincipal(OWNER_ID, AccountRole.OWNER));

        Instant ts = Instant.parse("2026-01-01T00:00:00Z");
        when(leadRepository.save(any(CrmLeadEntity.class))).thenAnswer(inv -> {
            CrmLeadEntity e = inv.getArgument(0);
            e.setId(LEAD_ID);
            e.setCreatedAt(ts);
            e.setUpdatedAt(ts);
            return e;
        });

        CreateCrmLeadRequestDto dto = new CreateCrmLeadRequestDto(
                "  Acme LLC  ",
                "  +1  ",
                null,
                "  comment  ",
                "  dept  ",
                CrmLeadStatus.NEW,
                null
        );

        CrmLeadResponseDto res = leadService.create(dto);

        assertThat(res.id()).isEqualTo(LEAD_ID);
        assertThat(res.companyName()).isEqualTo("Acme LLC");
        assertThat(res.phones()).isEqualTo("+1");
        assertThat(res.managerComment()).isEqualTo("comment");
        assertThat(res.department()).isEqualTo("dept");
        assertThat(res.assignedToAccountId()).isNull();

        ArgumentCaptor<CrmLeadEntity> captor = ArgumentCaptor.forClass(CrmLeadEntity.class);
        verify(leadRepository).save(captor.capture());
        CrmLeadEntity saved = captor.getValue();
        assertThat(saved.getAssignedTo()).isNull();
        assertThat(saved.getStatus()).isEqualTo(CrmLeadStatus.NEW);
    }

    @Test
    void create_shouldAssignAdmin_whenOwnerPassesAssignee() {
        when(crmAccessPolicy.requireCrmUser()).thenReturn(new AccountPrincipal(OWNER_ID, AccountRole.OWNER));

        AccountEntity admin = new AccountEntity();
        admin.setId(ADMIN_ID);
        admin.setRole(AccountRole.ADMIN);
        admin.setEmail("a@b.c");
        admin.setFirstName("");
        admin.setLastName("");
        when(accountRepository.findById(ADMIN_ID)).thenReturn(Optional.of(admin));

        Instant ts = Instant.parse("2026-01-02T00:00:00Z");
        when(leadRepository.save(any(CrmLeadEntity.class))).thenAnswer(inv -> {
            CrmLeadEntity e = inv.getArgument(0);
            e.setId(LEAD_ID);
            e.setCreatedAt(ts);
            e.setUpdatedAt(ts);
            return e;
        });

        leadService.create(new CreateCrmLeadRequestDto(
                "Co",
                null,
                null,
                null,
                null,
                null,
                ADMIN_ID
        ));

        ArgumentCaptor<CrmLeadEntity> captor = ArgumentCaptor.forClass(CrmLeadEntity.class);
        verify(leadRepository).save(captor.capture());
        assertThat(captor.getValue().getAssignedTo()).isSameAs(admin);
    }

    @Test
    void update_shouldPersistChanges_whenNotConverted() {
        when(crmAccessPolicy.requireCrmUser()).thenReturn(new AccountPrincipal(OWNER_ID, AccountRole.OWNER));

        AccountEntity admin = new AccountEntity();
        admin.setId(ADMIN_ID);
        admin.setRole(AccountRole.ADMIN);
        when(accountRepository.findById(ADMIN_ID)).thenReturn(Optional.of(admin));

        CrmLeadEntity existing = new CrmLeadEntity();
        existing.setId(LEAD_ID);
        existing.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        existing.setUpdatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        existing.setCompanyName("Old");
        existing.setStatus(CrmLeadStatus.NEW);
        existing.setAssignedTo(null);
        when(leadRepository.findById(LEAD_ID)).thenReturn(Optional.of(existing));
        when(leadRepository.save(existing)).thenReturn(existing);

        leadService.update(LEAD_ID, new UpdateCrmLeadRequestDto(
                "  NewCo  ",
                null,
                null,
                null,
                null,
                CrmLeadStatus.IN_PROGRESS,
                ADMIN_ID
        ));

        assertThat(existing.getCompanyName()).isEqualTo("NewCo");
        assertThat(existing.getStatus()).isEqualTo(CrmLeadStatus.IN_PROGRESS);
        assertThat(existing.getAssignedTo()).isSameAs(admin);
        verify(leadRepository).save(existing);
    }

    @Test
    void update_shouldThrow_whenLeadAlreadyConverted() {
        when(crmAccessPolicy.requireCrmUser()).thenReturn(new AccountPrincipal(OWNER_ID, AccountRole.OWNER));

        CrmLeadEntity existing = new CrmLeadEntity();
        existing.setId(LEAD_ID);
        existing.setStatus(CrmLeadStatus.CONVERTED);
        when(leadRepository.findById(LEAD_ID)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> leadService.update(LEAD_ID, new UpdateCrmLeadRequestDto(
                "X",
                null,
                null,
                null,
                null,
                CrmLeadStatus.IN_PROGRESS,
                null
        ))).isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void reassign_shouldPersistNewAssignee() {
        when(crmAccessPolicy.requireCrmUser()).thenReturn(new AccountPrincipal(OWNER_ID, AccountRole.OWNER));

        AccountEntity admin = new AccountEntity();
        admin.setId(ADMIN_ID);
        admin.setRole(AccountRole.ADMIN);
        when(accountRepository.findById(ADMIN_ID)).thenReturn(Optional.of(admin));

        CrmLeadEntity existing = new CrmLeadEntity();
        existing.setId(LEAD_ID);
        existing.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        existing.setUpdatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        existing.setStatus(CrmLeadStatus.IN_PROGRESS);
        existing.setAssignedTo(null);
        when(leadRepository.findById(LEAD_ID)).thenReturn(Optional.of(existing));
        when(leadRepository.save(existing)).thenReturn(existing);

        leadService.reassign(LEAD_ID, new CrmReassignLeadRequestDto(ADMIN_ID));

        assertThat(existing.getAssignedTo()).isSameAs(admin);
        verify(leadRepository).save(existing);
    }

    @Test
    void convertToOrganization_shouldSaveOrgAndMarkLeadConverted() {
        when(crmAccessPolicy.requireCrmUser()).thenReturn(new AccountPrincipal(OWNER_ID, AccountRole.OWNER));

        AccountEntity admin = new AccountEntity();
        admin.setId(ADMIN_ID);
        admin.setRole(AccountRole.ADMIN);

        CrmLeadEntity lead = new CrmLeadEntity();
        lead.setId(LEAD_ID);
        lead.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        lead.setUpdatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        lead.setCompanyName("LeadCo");
        lead.setPhones("+99");
        lead.setManagerComment("mgr");
        lead.setStatus(CrmLeadStatus.IN_PROGRESS);
        lead.setAssignedTo(admin);
        when(leadRepository.findById(LEAD_ID)).thenReturn(Optional.of(lead));

        when(organizationRepository.save(any(CrmOrganizationEntity.class))).thenAnswer(inv -> {
            CrmOrganizationEntity o = inv.getArgument(0);
            o.setId(NEW_ORG_ID);
            o.setCreatedAt(Instant.parse("2026-01-03T00:00:00Z"));
            o.setUpdatedAt(Instant.parse("2026-01-03T00:00:00Z"));
            return o;
        });
        when(leadRepository.save(lead)).thenReturn(lead);

        CrmOrganizationResponseDto orgDto = new CrmOrganizationResponseDto(
                NEW_ORG_ID,
                Instant.parse("2026-01-03T00:00:00Z"),
                Instant.parse("2026-01-03T00:00:00Z"),
                "LeadCo",
                null,
                null,
                null,
                null,
                null,
                "Phones: +99\nManager: mgr",
                null,
                null,
                null,
                null,
                CrmClientStatus.PROSPECT,
                null,
                null,
                ADMIN_ID,
                "Admin",
                null,
                null
        );
        when(organizationService.getById(NEW_ORG_ID)).thenReturn(orgDto);

        CrmOrganizationResponseDto result = leadService.convertToOrganization(LEAD_ID);

        assertThat(result).isEqualTo(orgDto);
        assertThat(lead.getStatus()).isEqualTo(CrmLeadStatus.CONVERTED);
        assertThat(lead.getConvertedOrganization()).isNotNull();
        assertThat(lead.getConvertedOrganization().getId()).isEqualTo(NEW_ORG_ID);

        ArgumentCaptor<CrmOrganizationEntity> orgCaptor = ArgumentCaptor.forClass(CrmOrganizationEntity.class);
        verify(organizationRepository).save(orgCaptor.capture());
        assertThat(orgCaptor.getValue().getName()).isEqualTo("LeadCo");
        assertThat(orgCaptor.getValue().getAssignedTo()).isSameAs(admin);

        verify(leadRepository).save(lead);
        verify(organizationService).getById(eq(NEW_ORG_ID));
    }
}
