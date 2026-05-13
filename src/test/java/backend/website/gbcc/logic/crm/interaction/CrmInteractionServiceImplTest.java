package backend.website.gbcc.logic.crm.interaction;

import backend.website.gbcc.logic.account.AccountEntity;
import backend.website.gbcc.logic.account.AccountRepository;
import backend.website.gbcc.logic.crm.access.CrmAccessPolicy;
import backend.website.gbcc.logic.crm.access.CrmEntityAccess;
import backend.website.gbcc.logic.crm.interaction.dto.CreateCrmInteractionRequestDto;
import backend.website.gbcc.logic.crm.interaction.dto.UpdateCrmInteractionRequestDto;
import backend.website.gbcc.logic.crm.lead.CrmLeadRepository;
import backend.website.gbcc.logic.crm.organization.CrmOrganizationEntity;
import backend.website.gbcc.logic.crm.organization.CrmOrganizationRepository;
import backend.website.gbcc.logic.crm.task.CrmTaskRepository;
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
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CrmInteractionServiceImplTest {

    private static final UUID OWNER_ID = UUID.fromString("00000000-0000-4000-8000-000000000021");
    private static final UUID ORG_ID = UUID.fromString("00000000-0000-4000-8000-000000000022");
    private static final UUID INTERACTION_ID = UUID.fromString("00000000-0000-4000-8000-000000000023");

    @Mock
    private CrmInteractionRepository interactionRepository;

    @Mock
    private CrmOrganizationRepository organizationRepository;

    @Mock
    private CrmLeadRepository leadRepository;

    @Mock
    private CrmTaskRepository taskRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CrmAccessPolicy crmAccessPolicy;

    @Mock
    private CrmEntityAccess crmEntityAccess;

    @InjectMocks
    private CrmInteractionServiceImpl interactionService;

    @Test
    void create_shouldPersistInteraction() {
        when(crmAccessPolicy.requireCrmUser()).thenReturn(new AccountPrincipal(OWNER_ID, AccountRole.OWNER));

        CrmOrganizationEntity org = new CrmOrganizationEntity();
        org.setId(ORG_ID);
        when(organizationRepository.findById(ORG_ID)).thenReturn(Optional.of(org));

        AccountEntity author = new AccountEntity();
        author.setId(OWNER_ID);
        author.setEmail("owner@example.com");
        author.setFirstName("");
        author.setLastName("");
        when(accountRepository.findById(OWNER_ID)).thenReturn(Optional.of(author));

        when(interactionRepository.save(any(CrmInteractionEntity.class))).thenAnswer(inv -> {
            CrmInteractionEntity e = inv.getArgument(0);
            e.setId(INTERACTION_ID);
            e.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
            e.setUpdatedAt(Instant.parse("2026-01-01T00:00:00Z"));
            return e;
        });

        Instant occurred = Instant.parse("2026-04-01T10:00:00Z");
        CreateCrmInteractionRequestDto dto = new CreateCrmInteractionRequestDto(
                ORG_ID,
                null,
                occurred,
                " result ",
                " comment ",
                " next ",
                null
        );

        var res = interactionService.create(dto);

        assertThat(res.id()).isEqualTo(INTERACTION_ID);
        assertThat(res.organizationId()).isEqualTo(ORG_ID);
        assertThat(res.resultNote()).isEqualTo("result");
        assertThat(res.commentText()).isEqualTo("comment");
        assertThat(res.nextStep()).isEqualTo("next");

        verify(crmEntityAccess).assertCanModifyInteractionTarget(same(org), isNull());

        ArgumentCaptor<CrmInteractionEntity> captor = ArgumentCaptor.forClass(CrmInteractionEntity.class);
        verify(interactionRepository).save(captor.capture());
        CrmInteractionEntity saved = captor.getValue();
        assertThat(saved.getAuthor()).isSameAs(author);
        assertThat(saved.getOccurredAt()).isEqualTo(occurred);
        assertThat(saved.getNextTask()).isNull();
    }

    @Test
    void update_shouldSaveChanges() {
        when(crmAccessPolicy.requireCrmUser()).thenReturn(new AccountPrincipal(OWNER_ID, AccountRole.OWNER));

        AccountEntity author = new AccountEntity();
        author.setId(OWNER_ID);
        author.setEmail("o@e.com");
        author.setFirstName("");
        author.setLastName("");

        CrmOrganizationEntity org = new CrmOrganizationEntity();
        org.setId(ORG_ID);

        CrmInteractionEntity existing = new CrmInteractionEntity();
        existing.setId(INTERACTION_ID);
        existing.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        existing.setUpdatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        existing.setAuthor(author);
        existing.setOrganization(org);
        existing.setLead(null);
        when(interactionRepository.findById(INTERACTION_ID)).thenReturn(Optional.of(existing));
        when(organizationRepository.findById(ORG_ID)).thenReturn(Optional.of(org));
        when(interactionRepository.save(existing)).thenReturn(existing);

        Instant newOccurred = Instant.parse("2026-05-05T15:00:00Z");
        UpdateCrmInteractionRequestDto dto = new UpdateCrmInteractionRequestDto(
                ORG_ID,
                null,
                newOccurred,
                "r2",
                "c2",
                "n2",
                null
        );

        var res = interactionService.update(INTERACTION_ID, dto);

        assertThat(res.resultNote()).isEqualTo("r2");
        assertThat(existing.getOccurredAt()).isEqualTo(newOccurred);
        verify(interactionRepository).save(existing);
    }

    @Test
    void delete_shouldCallRepositoryDelete() {
        when(crmAccessPolicy.requireCrmUser()).thenReturn(new AccountPrincipal(OWNER_ID, AccountRole.OWNER));

        AccountEntity author = new AccountEntity();
        author.setId(OWNER_ID);

        CrmInteractionEntity existing = new CrmInteractionEntity();
        existing.setId(INTERACTION_ID);
        existing.setAuthor(author);
        when(interactionRepository.findById(INTERACTION_ID)).thenReturn(Optional.of(existing));

        interactionService.delete(INTERACTION_ID);

        verify(interactionRepository).delete(existing);
    }
}
