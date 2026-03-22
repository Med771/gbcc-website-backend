package backend.website.gbcc.logic.contactrequest;

import backend.website.gbcc.helper.SecurityContextHelper;
import backend.website.gbcc.logic.account.AccountEntity;
import backend.website.gbcc.logic.account.AccountRepository;
import backend.website.gbcc.logic.contactrequest.dto.ContactRequestResponseDto;
import backend.website.gbcc.logic.contactrequest.dto.CreateContactRequestDto;
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
class ContactRequestServiceImplTest {

    @Mock
    private ContactRequestRepository contactRequestRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private ContactRequestMapper contactRequestMapper;

    @Spy
    private SecurityContextHelper securityContextHelper = new SecurityContextHelper();

    @InjectMocks
    private ContactRequestServiceImpl contactRequestService;

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void create_persistsAndReturnsResponse() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        when(contactRequestRepository.save(any(ContactRequestEntity.class))).thenAnswer(inv -> {
            ContactRequestEntity e = inv.getArgument(0);
            e.setId(id);
            e.setCreatedAt(now);
            e.setUpdatedAt(now);
            return e;
        });
        when(contactRequestMapper.toResponse(any(ContactRequestEntity.class))).thenReturn(
                new ContactRequestResponseDto(id, "Ivan", "ivan@test.com", null, "Hello", true,
                        null, null, null, null, now, now)
        );

        ContactRequestResponseDto response = contactRequestService.create(new CreateContactRequestDto(
                "Ivan", "ivan@test.com", null, "Hello", true
        ));

        assertThat(response.id()).isEqualTo(id);
        assertThat(response.name()).isEqualTo("Ivan");

        ArgumentCaptor<ContactRequestEntity> captor = ArgumentCaptor.forClass(ContactRequestEntity.class);
        verify(contactRequestRepository).save(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo("ivan@test.com");
        assertThat(captor.getValue().getConsentProcessing()).isTrue();
    }

    @Test
    void take_assignsCurrentAdmin() {
        UUID adminId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();
        setPrincipal(adminId, AccountRole.ADMIN);

        ContactRequestEntity entity = new ContactRequestEntity();
        entity.setId(requestId);
        entity.setName("A");
        entity.setEmail("a@test.com");
        entity.setMessage("m");
        entity.setConsentProcessing(true);
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());

        AccountEntity admin = new AccountEntity();
        admin.setId(adminId);
        admin.setEmail("admin@test.com");
        admin.setName("Admin");

        when(contactRequestRepository.findById(requestId)).thenReturn(Optional.of(entity));
        when(accountRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(contactRequestRepository.save(any(ContactRequestEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(contactRequestMapper.toResponse(any(ContactRequestEntity.class))).thenAnswer(inv -> {
            ContactRequestEntity e = inv.getArgument(0);
            return new ContactRequestResponseDto(
                    e.getId(), e.getName(), e.getEmail(), e.getPhone(), e.getMessage(), e.getConsentProcessing(),
                    adminId, admin.getName(), admin.getEmail(), e.getAssignedAt(),
                    e.getCreatedAt(), e.getUpdatedAt()
            );
        });

        ContactRequestResponseDto response = contactRequestService.take(requestId);
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

        ContactRequestEntity entity = new ContactRequestEntity();
        entity.setId(requestId);
        entity.setAssignedTo(other);
        entity.setAssignedAt(Instant.now());

        when(contactRequestRepository.findById(requestId)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> contactRequestService.take(requestId))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode().value()).isEqualTo(CONFLICT.value()));
    }

    @Test
    void searchForAdmin_requiresManager() {
        UUID customerId = UUID.randomUUID();
        setPrincipal(customerId, AccountRole.CUSTOMER);

        assertThatThrownBy(() -> contactRequestService.searchForAdmin(null, null, PageRequest.of(0, 20)))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode().value()).isEqualTo(FORBIDDEN.value()));
    }

    @Test
    void searchForAdmin_returnsPage() {
        UUID ownerId = UUID.randomUUID();
        setPrincipal(ownerId, AccountRole.OWNER);
        when(contactRequestRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 20), 0));

        assertThat(contactRequestService.searchForAdmin(true, "q", PageRequest.of(0, 20)).data())
                .isEqualTo(List.of());
    }

    private void setPrincipal(UUID accountId, AccountRole role) {
        AccountPrincipal principal = new AccountPrincipal(accountId, role);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList())
        );
    }
}
