package backend.website.gbcc.logic.crm.task;

import backend.website.gbcc.logic.account.AccountEntity;
import backend.website.gbcc.logic.account.AccountRepository;
import backend.website.gbcc.logic.crm.access.CrmAccessPolicy;
import backend.website.gbcc.logic.crm.lead.CrmLeadRepository;
import backend.website.gbcc.logic.crm.organization.CrmOrganizationEntity;
import backend.website.gbcc.logic.crm.organization.CrmOrganizationRepository;
import backend.website.gbcc.logic.crm.task.dto.CreateCrmTaskRequestDto;
import backend.website.gbcc.logic.crm.task.dto.UpdateCrmTaskRequestDto;
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
class CrmTaskServiceImplTest {

    private static final UUID OWNER_ID = UUID.fromString("00000000-0000-4000-8000-000000000011");
    private static final UUID ADMIN_ID = UUID.fromString("00000000-0000-4000-8000-000000000012");
    private static final UUID ORG_ID = UUID.fromString("00000000-0000-4000-8000-000000000013");
    private static final UUID TASK_ID = UUID.fromString("00000000-0000-4000-8000-000000000014");

    @Mock
    private CrmTaskRepository taskRepository;

    @Mock
    private CrmOrganizationRepository organizationRepository;

    @Mock
    private CrmLeadRepository leadRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CrmAccessPolicy crmAccessPolicy;

    @InjectMocks
    private CrmTaskServiceImpl taskService;

    @Test
    void create_shouldPersistTask_forOwnerWithAdminAssignee() {
        when(crmAccessPolicy.requireCrmUser()).thenReturn(new AccountPrincipal(OWNER_ID, AccountRole.OWNER));

        CrmOrganizationEntity org = new CrmOrganizationEntity();
        org.setId(ORG_ID);
        org.setAssignedTo(null);
        when(organizationRepository.findById(ORG_ID)).thenReturn(Optional.of(org));

        AccountEntity admin = new AccountEntity();
        admin.setId(ADMIN_ID);
        admin.setRole(AccountRole.ADMIN);
        admin.setEmail("admin@example.com");
        admin.setFirstName("");
        admin.setLastName("");
        when(accountRepository.findById(ADMIN_ID)).thenReturn(Optional.of(admin));

        Instant dueAt = Instant.parse("2026-06-15T12:00:00Z");
        when(taskRepository.save(any(CrmTaskEntity.class))).thenAnswer(inv -> {
            CrmTaskEntity e = inv.getArgument(0);
            e.setId(TASK_ID);
            e.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
            e.setUpdatedAt(Instant.parse("2026-01-01T00:00:00Z"));
            return e;
        });

        CreateCrmTaskRequestDto dto = new CreateCrmTaskRequestDto(
                ORG_ID,
                null,
                dueAt,
                "  reason  ",
                "  comment  ",
                CrmTaskStatus.OPEN,
                ADMIN_ID
        );

        var res = taskService.create(dto);

        assertThat(res.id()).isEqualTo(TASK_ID);
        assertThat(res.organizationId()).isEqualTo(ORG_ID);
        assertThat(res.assigneeAccountId()).isEqualTo(ADMIN_ID);
        assertThat(res.reason()).isEqualTo("reason");
        assertThat(res.commentText()).isEqualTo("comment");

        ArgumentCaptor<CrmTaskEntity> captor = ArgumentCaptor.forClass(CrmTaskEntity.class);
        verify(taskRepository).save(captor.capture());
        CrmTaskEntity saved = captor.getValue();
        assertThat(saved.getOrganization()).isSameAs(org);
        assertThat(saved.getLead()).isNull();
        assertThat(saved.getDueAt()).isEqualTo(dueAt);
        assertThat(saved.getAssignee()).isSameAs(admin);
        assertThat(saved.getStatus()).isEqualTo(CrmTaskStatus.OPEN);
    }

    @Test
    void update_shouldPersistChanges() {
        when(crmAccessPolicy.requireCrmUser()).thenReturn(new AccountPrincipal(OWNER_ID, AccountRole.OWNER));

        AccountEntity admin = new AccountEntity();
        admin.setId(ADMIN_ID);
        admin.setRole(AccountRole.ADMIN);
        admin.setEmail("a@b.c");
        admin.setFirstName("");
        admin.setLastName("");

        CrmOrganizationEntity org = new CrmOrganizationEntity();
        org.setId(ORG_ID);
        org.setAssignedTo(null);

        CrmTaskEntity existing = new CrmTaskEntity();
        existing.setId(TASK_ID);
        existing.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        existing.setUpdatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        existing.setOrganization(org);
        existing.setLead(null);
        existing.setAssignee(admin);
        existing.setDueAt(Instant.parse("2026-05-01T00:00:00Z"));
        existing.setStatus(CrmTaskStatus.OPEN);
        existing.setReason("old");
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(existing));
        when(organizationRepository.findById(ORG_ID)).thenReturn(Optional.of(org));
        when(accountRepository.findById(ADMIN_ID)).thenReturn(Optional.of(admin));
        when(taskRepository.save(existing)).thenReturn(existing);

        Instant newDue = Instant.parse("2026-07-01T00:00:00Z");
        UpdateCrmTaskRequestDto dto = new UpdateCrmTaskRequestDto(
                ORG_ID,
                null,
                newDue,
                "r2",
                "c2",
                CrmTaskStatus.DONE,
                ADMIN_ID
        );

        var res = taskService.update(TASK_ID, dto);

        assertThat(res.status()).isEqualTo(CrmTaskStatus.DONE);
        assertThat(res.dueAt()).isEqualTo(newDue);
        assertThat(existing.getReason()).isEqualTo("r2");
        verify(taskRepository).save(existing);
    }

    @Test
    void delete_shouldCallRepositoryDelete() {
        when(crmAccessPolicy.requireCrmUser()).thenReturn(new AccountPrincipal(OWNER_ID, AccountRole.OWNER));

        AccountEntity admin = new AccountEntity();
        admin.setId(ADMIN_ID);
        admin.setRole(AccountRole.ADMIN);

        CrmTaskEntity existing = new CrmTaskEntity();
        existing.setId(TASK_ID);
        existing.setAssignee(admin);
        existing.setDueAt(Instant.parse("2026-08-01T00:00:00Z"));
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(existing));

        taskService.delete(TASK_ID);

        verify(taskRepository).delete(existing);
    }
}
