package backend.website.gbcc.logic.crm.organization;

import backend.website.gbcc.logic.account.AccountEntity;
import backend.website.gbcc.logic.account.AccountRepository;
import backend.website.gbcc.logic.contactrequest.ContactRequestRepository;
import backend.website.gbcc.logic.cooperationrequest.CooperationRequestRepository;
import backend.website.gbcc.logic.crm.access.CrmAccessPolicy;
import backend.website.gbcc.logic.crm.organization.dto.CreateCrmOrganizationContactRequestDto;
import backend.website.gbcc.logic.crm.organization.dto.UpdateCrmOrganizationContactRequestDto;
import backend.website.gbcc.model.AccountPrincipal;
import backend.website.gbcc.model.AccountRole;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CrmOrganizationServiceImplTest {

    private static final UUID OWNER_ID = UUID.fromString("00000000-0000-4000-8000-000000000051");
    private static final UUID ORG_ID = UUID.fromString("00000000-0000-4000-8000-000000000052");
    private static final UUID CONTACT_ID = UUID.fromString("00000000-0000-4000-8000-000000000053");

    @Mock
    private CrmOrganizationRepository organizationRepository;

    @Mock
    private CrmOrganizationContactRepository contactRepository;

    @Mock
    private CrmOrganizationContactHistoryRepository historyRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ContactRequestRepository contactRequestRepository;

    @Mock
    private CooperationRequestRepository cooperationRequestRepository;

    @Mock
    private CrmAccessPolicy crmAccessPolicy;

    @InjectMocks
    private CrmOrganizationServiceImpl organizationService;

    @Test
    void addContact_shouldPersistViaContactRepository() {
        when(crmAccessPolicy.requireCrmUser()).thenReturn(new AccountPrincipal(OWNER_ID, AccountRole.OWNER));

        CrmOrganizationEntity org = new CrmOrganizationEntity();
        org.setId(ORG_ID);
        org.setAssignedTo(null);
        when(organizationRepository.findById(ORG_ID)).thenReturn(Optional.of(org));

        when(contactRepository.save(any(CrmOrganizationContactEntity.class))).thenAnswer(inv -> {
            CrmOrganizationContactEntity c = inv.getArgument(0);
            c.setId(CONTACT_ID);
            c.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
            c.setUpdatedAt(Instant.parse("2026-01-01T00:00:00Z"));
            return c;
        });

        var res = organizationService.addContact(ORG_ID, new CreateCrmOrganizationContactRequestDto(
                "  Ivan  ",
                "  Sales  ",
                "+7000",
                " ivan@x.test ",
                "  note  "
        ));

        assertThat(res.id()).isEqualTo(CONTACT_ID);
        assertThat(res.fullName()).isEqualTo("Ivan");
        assertThat(res.department()).isEqualTo("Sales");
        assertThat(res.phone()).isEqualTo("+7000");
        assertThat(res.email()).isEqualTo("ivan@x.test");
        assertThat(res.extraNote()).isEqualTo("note");

        ArgumentCaptor<CrmOrganizationContactEntity> captor = ArgumentCaptor.forClass(CrmOrganizationContactEntity.class);
        verify(contactRepository).save(captor.capture());
        assertThat(captor.getValue().getOrganization()).isSameAs(org);
    }

    @Test
    void deleteContact_shouldCallRepositoryDelete() {
        when(crmAccessPolicy.requireCrmUser()).thenReturn(new AccountPrincipal(OWNER_ID, AccountRole.OWNER));

        CrmOrganizationEntity org = new CrmOrganizationEntity();
        org.setId(ORG_ID);
        org.setAssignedTo(null);
        when(organizationRepository.findById(ORG_ID)).thenReturn(Optional.of(org));

        CrmOrganizationContactEntity contact = new CrmOrganizationContactEntity();
        contact.setId(CONTACT_ID);
        contact.setOrganization(org);
        when(contactRepository.findById(CONTACT_ID)).thenReturn(Optional.of(contact));

        organizationService.deleteContact(ORG_ID, CONTACT_ID);

        verify(contactRepository).delete(contact);
    }

    @Test
    void updateContact_whenSnapshotChanges_shouldSaveHistory() {
        when(crmAccessPolicy.requireCrmUser()).thenReturn(new AccountPrincipal(OWNER_ID, AccountRole.OWNER));

        CrmOrganizationEntity org = new CrmOrganizationEntity();
        org.setId(ORG_ID);
        org.setAssignedTo(null);
        when(organizationRepository.findById(ORG_ID)).thenReturn(Optional.of(org));

        CrmOrganizationContactEntity contact = new CrmOrganizationContactEntity();
        contact.setId(CONTACT_ID);
        contact.setOrganization(org);
        contact.setFullName("OldName");
        contact.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        contact.setUpdatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        when(contactRepository.findById(CONTACT_ID)).thenReturn(Optional.of(contact));
        when(contactRepository.save(contact)).thenReturn(contact);

        AccountEntity changer = new AccountEntity();
        changer.setId(OWNER_ID);
        changer.setEmail("owner@example.com");
        changer.setFirstName("");
        changer.setLastName("");
        when(accountRepository.findById(OWNER_ID)).thenReturn(Optional.of(changer));

        when(historyRepository.save(any(CrmOrganizationContactHistoryEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        organizationService.updateContact(ORG_ID, CONTACT_ID, new UpdateCrmOrganizationContactRequestDto(
                "NewName",
                null,
                null,
                null,
                null
        ));

        assertThat(contact.getFullName()).isEqualTo("NewName");

        ArgumentCaptor<CrmOrganizationContactHistoryEntity> histCaptor =
                ArgumentCaptor.forClass(CrmOrganizationContactHistoryEntity.class);
        verify(historyRepository).save(histCaptor.capture());
        CrmOrganizationContactHistoryEntity h = histCaptor.getValue();
        assertThat(h.getContact()).isSameAs(contact);
        assertThat(h.getChangedBy()).isSameAs(changer);
        assertThat(h.getPreviousSnapshot()).contains("OldName");
        assertThat(h.getNewSnapshot()).contains("NewName");

        verify(contactRepository).save(contact);
    }

    @Test
    void updateContact_whenSnapshotUnchanged_shouldNotSaveHistory() {
        when(crmAccessPolicy.requireCrmUser()).thenReturn(new AccountPrincipal(OWNER_ID, AccountRole.OWNER));

        CrmOrganizationEntity org = new CrmOrganizationEntity();
        org.setId(ORG_ID);
        org.setAssignedTo(null);
        when(organizationRepository.findById(ORG_ID)).thenReturn(Optional.of(org));

        CrmOrganizationContactEntity contact = new CrmOrganizationContactEntity();
        contact.setId(CONTACT_ID);
        contact.setOrganization(org);
        contact.setFullName("Same");
        contact.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        contact.setUpdatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        when(contactRepository.findById(CONTACT_ID)).thenReturn(Optional.of(contact));
        when(contactRepository.save(contact)).thenReturn(contact);

        organizationService.updateContact(ORG_ID, CONTACT_ID, new UpdateCrmOrganizationContactRequestDto(
                "  Same  ",
                null,
                null,
                null,
                null
        ));

        verify(contactRepository).save(contact);
        verify(historyRepository, never()).save(any());
    }
}
