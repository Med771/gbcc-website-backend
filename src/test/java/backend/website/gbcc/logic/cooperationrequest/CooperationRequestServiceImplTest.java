package backend.website.gbcc.logic.cooperationrequest;

import backend.website.gbcc.helper.SecurityContextHelper;
import backend.website.gbcc.logic.account.AccountEntity;
import backend.website.gbcc.logic.account.AccountRepository;
import backend.website.gbcc.logic.cooperationrequest.dto.CooperationRequestResponseDto;
import backend.website.gbcc.logic.cooperationrequest.dto.CreateCooperationRequestDto;
import backend.website.gbcc.logic.file.FileRepository;
import backend.website.gbcc.model.AccountPrincipal;
import backend.website.gbcc.model.AccountRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;

@ExtendWith(MockitoExtension.class)
class CooperationRequestServiceImplTest {

    @Mock
    private CooperationRequestRepository cooperationRequestRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private FileRepository fileRepository;
    @Mock
    private CooperationRequestMapper cooperationRequestMapper;

    @Spy
    private SecurityContextHelper securityContextHelper = new SecurityContextHelper();

    @InjectMocks
    private CooperationRequestServiceImpl cooperationRequestService;

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void create_persistsAndReturnsResponse() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        when(cooperationRequestRepository.save(any(CooperationRequestEntity.class))).thenAnswer(inv -> {
            CooperationRequestEntity e = inv.getArgument(0);
            e.setId(id);
            e.setCreatedAt(now);
            e.setUpdatedAt(now);
            return e;
        });
        when(cooperationRequestMapper.toResponse(any(CooperationRequestEntity.class))).thenReturn(
                new CooperationRequestResponseDto(id, "Ivan", "+79990000000", "ivan@test.com", "dealer", null, null, true,
                        null, null, null, null, now, now)
        );

        CooperationRequestResponseDto response = cooperationRequestService.create(new CreateCooperationRequestDto(
                "Ivan", "+79990000000", "ivan@test.com", "dealer", null, null, true
        ));

        assertThat(response.id()).isEqualTo(id);
        assertThat(response.phone()).isEqualTo("+79990000000");

        ArgumentCaptor<CooperationRequestEntity> captor = ArgumentCaptor.forClass(CooperationRequestEntity.class);
        verify(cooperationRequestRepository).save(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo("ivan@test.com");
        assertThat(captor.getValue().getConsentProcessing()).isTrue();
    }

    @Test
    void take_assignsCurrentAdmin() {
        UUID adminId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();
        setPrincipal(adminId, AccountRole.ADMIN);

        CooperationRequestEntity entity = new CooperationRequestEntity();
        entity.setId(requestId);
        entity.setName("A");
        entity.setPhone("+1");
        entity.setEmail("a@test.com");
        entity.setConsentProcessing(true);
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());

        AccountEntity admin = new AccountEntity();
        admin.setId(adminId);
        admin.setEmail("admin@test.com");
        admin.setName("Admin");

        when(cooperationRequestRepository.findById(requestId)).thenReturn(Optional.of(entity));
        when(accountRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(cooperationRequestRepository.save(any(CooperationRequestEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(cooperationRequestMapper.toResponse(any(CooperationRequestEntity.class))).thenAnswer(inv -> {
            CooperationRequestEntity e = inv.getArgument(0);
            return new CooperationRequestResponseDto(
                    e.getId(), e.getName(), e.getPhone(), e.getEmail(), e.getCooperationType(), e.getComment(),
                    null, e.getConsentProcessing(),
                    adminId, admin.getName(), admin.getEmail(), e.getAssignedAt(),
                    e.getCreatedAt(), e.getUpdatedAt()
            );
        });

        CooperationRequestResponseDto response = cooperationRequestService.take(requestId);
        assertThat(response.assignedToAccountId()).isEqualTo(adminId);
        assertThat(response.assignedToEmail()).isEqualTo("admin@test.com");
    }

    @Test
    void take_whenAssignedToOtherAdmin_throwsConflict() {
        UUID adminId = UUID.randomUUID();
        UUID otherId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();
        setPrincipal(adminId, AccountRole.ADMIN);

        AccountEntity other = new AccountEntity();
        other.setId(otherId);

        CooperationRequestEntity entity = new CooperationRequestEntity();
        entity.setId(requestId);
        entity.setAssignedTo(other);
        entity.setAssignedAt(Instant.now());

        when(cooperationRequestRepository.findById(requestId)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> cooperationRequestService.take(requestId))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode().value()).isEqualTo(CONFLICT.value()));
    }

    @Test
    void searchForAdmin_requiresManager() {
        UUID customerId = UUID.randomUUID();
        setPrincipal(customerId, AccountRole.CUSTOMER);

        assertThatThrownBy(() -> cooperationRequestService.searchForAdmin(null, null, PageRequest.of(0, 20)))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode().value()).isEqualTo(FORBIDDEN.value()));
    }

    @Test
    void searchForAdmin_returnsPage() {
        UUID ownerId = UUID.randomUUID();
        setPrincipal(ownerId, AccountRole.OWNER);
        when(cooperationRequestRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 20), 0));

        assertThat(cooperationRequestService.searchForAdmin(true, "q", PageRequest.of(0, 20)).data())
                .isEqualTo(List.of());
    }

    private void setPrincipal(UUID accountId, AccountRole role) {
        AccountPrincipal principal = new AccountPrincipal(accountId, role);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList())
        );
    }
}
