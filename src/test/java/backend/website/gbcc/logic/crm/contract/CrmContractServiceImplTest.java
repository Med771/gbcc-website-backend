package backend.website.gbcc.logic.crm.contract;

import backend.website.gbcc.logic.crm.access.CrmAccessPolicy;
import backend.website.gbcc.logic.crm.contract.dto.CreateCrmContractLineRequestDto;
import backend.website.gbcc.logic.crm.contract.dto.CreateCrmContractRequestDto;
import backend.website.gbcc.logic.crm.contract.dto.UpdateCrmContractLineRequestDto;
import backend.website.gbcc.logic.crm.contract.dto.UpdateCrmContractRequestDto;
import backend.website.gbcc.logic.crm.organization.CrmOrganizationEntity;
import backend.website.gbcc.logic.crm.organization.CrmOrganizationRepository;
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
class CrmContractServiceImplTest {

    private static final UUID OWNER_ID = UUID.fromString("00000000-0000-4000-8000-000000000041");
    private static final UUID ORG_ID = UUID.fromString("00000000-0000-4000-8000-000000000042");
    private static final UUID CONTRACT_ID = UUID.fromString("00000000-0000-4000-8000-000000000043");
    private static final UUID LINE_ID = UUID.fromString("00000000-0000-4000-8000-000000000044");

    @Mock
    private CrmContractRepository contractRepository;

    @Mock
    private CrmContractLineRepository lineRepository;

    @Mock
    private CrmOrganizationRepository organizationRepository;

    @Mock
    private CrmAccessPolicy crmAccessPolicy;

    @InjectMocks
    private CrmContractServiceImpl contractService;

    @Test
    void create_shouldPersistContract() {
        when(crmAccessPolicy.requireCrmUser()).thenReturn(new AccountPrincipal(OWNER_ID, AccountRole.OWNER));

        CrmOrganizationEntity org = new CrmOrganizationEntity();
        org.setId(ORG_ID);
        org.setAssignedTo(null);
        when(organizationRepository.findById(ORG_ID)).thenReturn(Optional.of(org));

        when(contractRepository.save(any(CrmContractEntity.class))).thenAnswer(inv -> {
            CrmContractEntity e = inv.getArgument(0);
            e.setId(CONTRACT_ID);
            e.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
            e.setUpdatedAt(Instant.parse("2026-01-01T00:00:00Z"));
            return e;
        });

        CreateCrmContractRequestDto dto = new CreateCrmContractRequestDto(
                ORG_ID,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                "  terms  "
        );

        var res = contractService.create(dto);

        assertThat(res.id()).isEqualTo(CONTRACT_ID);
        assertThat(res.commentText()).isEqualTo("terms");

        ArgumentCaptor<CrmContractEntity> captor = ArgumentCaptor.forClass(CrmContractEntity.class);
        verify(contractRepository).save(captor.capture());
        CrmContractEntity saved = captor.getValue();
        assertThat(saved.getOrganization()).isSameAs(org);
        assertThat(saved.getStartDate()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(saved.getEndDate()).isEqualTo(LocalDate.of(2026, 12, 31));
    }

    @Test
    void update_shouldSaveMutatedContract() {
        when(crmAccessPolicy.requireCrmUser()).thenReturn(new AccountPrincipal(OWNER_ID, AccountRole.OWNER));

        CrmOrganizationEntity org = new CrmOrganizationEntity();
        org.setId(ORG_ID);
        org.setAssignedTo(null);

        CrmContractEntity existing = new CrmContractEntity();
        existing.setId(CONTRACT_ID);
        existing.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        existing.setUpdatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        existing.setOrganization(org);
        existing.setCommentText("old");
        when(contractRepository.findById(CONTRACT_ID)).thenReturn(Optional.of(existing));
        when(contractRepository.save(existing)).thenReturn(existing);

        contractService.update(CONTRACT_ID, new UpdateCrmContractRequestDto(
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 3, 1),
                " new c "
        ));

        assertThat(existing.getCommentText()).isEqualTo("new c");
        verify(contractRepository).save(existing);
    }

    @Test
    void delete_shouldCallRepositoryDelete() {
        when(crmAccessPolicy.requireCrmUser()).thenReturn(new AccountPrincipal(OWNER_ID, AccountRole.OWNER));

        CrmOrganizationEntity org = new CrmOrganizationEntity();
        org.setId(ORG_ID);
        org.setAssignedTo(null);

        CrmContractEntity existing = new CrmContractEntity();
        existing.setId(CONTRACT_ID);
        existing.setOrganization(org);
        when(contractRepository.findById(CONTRACT_ID)).thenReturn(Optional.of(existing));

        contractService.delete(CONTRACT_ID);

        verify(contractRepository).delete(existing);
    }

    @Test
    void addLine_shouldPersistLineLinkedToContract() {
        when(crmAccessPolicy.requireCrmUser()).thenReturn(new AccountPrincipal(OWNER_ID, AccountRole.OWNER));

        CrmOrganizationEntity org = new CrmOrganizationEntity();
        org.setId(ORG_ID);
        org.setAssignedTo(null);

        CrmContractEntity contract = new CrmContractEntity();
        contract.setId(CONTRACT_ID);
        contract.setOrganization(org);
        when(contractRepository.findById(CONTRACT_ID)).thenReturn(Optional.of(contract));

        when(lineRepository.save(any(CrmContractLineEntity.class))).thenAnswer(inv -> {
            CrmContractLineEntity line = inv.getArgument(0);
            line.setId(LINE_ID);
            line.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
            line.setUpdatedAt(Instant.parse("2026-01-01T00:00:00Z"));
            return line;
        });

        var res = contractService.addLine(CONTRACT_ID, new CreateCrmContractLineRequestDto(
                LocalDate.of(2026, 4, 1),
                new BigDecimal("10.5"),
                CrmContractLineStatus.PENDING,
                "  line note  "
        ));

        assertThat(res.id()).isEqualTo(LINE_ID);
        assertThat(res.contractId()).isEqualTo(CONTRACT_ID);
        assertThat(res.commentText()).isEqualTo("line note");

        ArgumentCaptor<CrmContractLineEntity> captor = ArgumentCaptor.forClass(CrmContractLineEntity.class);
        verify(lineRepository).save(captor.capture());
        CrmContractLineEntity saved = captor.getValue();
        assertThat(saved.getContract()).isSameAs(contract);
        assertThat(saved.getPlannedDate()).isEqualTo(LocalDate.of(2026, 4, 1));
        assertThat(saved.getPlannedQuantity()).isEqualByComparingTo(new BigDecimal("10.5"));
        assertThat(saved.getLineStatus()).isEqualTo(CrmContractLineStatus.PENDING);
    }

    @Test
    void updateLine_shouldSaveMutatedLine() {
        when(crmAccessPolicy.requireCrmUser()).thenReturn(new AccountPrincipal(OWNER_ID, AccountRole.OWNER));

        CrmOrganizationEntity org = new CrmOrganizationEntity();
        org.setId(ORG_ID);
        org.setAssignedTo(null);

        CrmContractEntity contract = new CrmContractEntity();
        contract.setId(CONTRACT_ID);
        contract.setOrganization(org);

        CrmContractLineEntity line = new CrmContractLineEntity();
        line.setId(LINE_ID);
        line.setContract(contract);
        line.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        line.setUpdatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        line.setPlannedDate(LocalDate.of(2026, 1, 1));
        line.setPlannedQuantity(BigDecimal.ONE);
        line.setLineStatus(CrmContractLineStatus.PENDING);
        line.setCommentText("old");

        when(contractRepository.findById(CONTRACT_ID)).thenReturn(Optional.of(contract));
        when(lineRepository.findById(LINE_ID)).thenReturn(Optional.of(line));
        when(lineRepository.save(line)).thenReturn(line);

        contractService.updateLine(CONTRACT_ID, LINE_ID, new UpdateCrmContractLineRequestDto(
                LocalDate.of(2026, 6, 1),
                new BigDecimal("2"),
                CrmContractLineStatus.DONE,
                " new "
        ));

        assertThat(line.getLineStatus()).isEqualTo(CrmContractLineStatus.DONE);
        assertThat(line.getCommentText()).isEqualTo("new");
        verify(lineRepository).save(line);
    }

    @Test
    void deleteLine_shouldCallRepositoryDelete() {
        when(crmAccessPolicy.requireCrmUser()).thenReturn(new AccountPrincipal(OWNER_ID, AccountRole.OWNER));

        CrmOrganizationEntity org = new CrmOrganizationEntity();
        org.setId(ORG_ID);
        org.setAssignedTo(null);

        CrmContractEntity contract = new CrmContractEntity();
        contract.setId(CONTRACT_ID);
        contract.setOrganization(org);

        CrmContractLineEntity line = new CrmContractLineEntity();
        line.setId(LINE_ID);
        line.setContract(contract);

        when(contractRepository.findById(CONTRACT_ID)).thenReturn(Optional.of(contract));
        when(lineRepository.findById(LINE_ID)).thenReturn(Optional.of(line));

        contractService.deleteLine(CONTRACT_ID, LINE_ID);

        verify(lineRepository).delete(line);
    }
}
