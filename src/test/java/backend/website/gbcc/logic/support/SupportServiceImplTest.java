package backend.website.gbcc.logic.support;

import backend.website.gbcc.helper.SecurityContextHelper;
import backend.website.gbcc.logic.account.AccountEntity;
import backend.website.gbcc.logic.account.AccountRepository;
import backend.website.gbcc.logic.support.dto.AddSupportMessageRequestDto;
import backend.website.gbcc.logic.support.dto.CreateSupportConversationRequestDto;
import backend.website.gbcc.logic.support.dto.SupportConversationDetailResponseDto;
import backend.website.gbcc.logic.support.dto.UpdateSupportConversationStatusRequestDto;
import backend.website.gbcc.model.AccountPrincipal;
import backend.website.gbcc.model.AccountRole;
import backend.website.gbcc.model.SupportConversationStatus;
import backend.website.gbcc.model.SupportMessageAuthorType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupportServiceImplTest {

    @Mock
    private SupportConversationRepository supportConversationRepository;
    @Mock
    private SupportMessageRepository supportMessageRepository;
    @Mock
    private AccountRepository accountRepository;
    @Spy
    private SecurityContextHelper securityContextHelper = new SecurityContextHelper();
    @Mock
    private SupportMapper supportMapper;

    @InjectMocks
    private SupportServiceImpl supportService;

    @Test
    void createConversation_asGuest_persistsGuestThreadAndToken() {
        when(securityContextHelper.getCurrentAccountPrincipal()).thenReturn(Optional.empty());
        when(supportConversationRepository.save(any(SupportConversationEntity.class))).thenAnswer(inv -> {
            SupportConversationEntity e = inv.getArgument(0);
            e.setId(UUID.randomUUID());
            e.setCreatedAt(java.time.Instant.now());
            e.setUpdatedAt(java.time.Instant.now());
            return e;
        });

        var response = supportService.createConversation(new CreateSupportConversationRequestDto(
                "Help",
                "Hello",
                "Ivan",
                "ivan@test.com"
        ));

        assertThat(response.guestAccessToken()).isNotBlank();
        assertThat(response.guestAccessToken()).hasSize(64);

        ArgumentCaptor<SupportConversationEntity> convCaptor = ArgumentCaptor.forClass(SupportConversationEntity.class);
        verify(supportConversationRepository).save(convCaptor.capture());
        SupportConversationEntity saved = convCaptor.getValue();
        assertThat(saved.getGuestName()).isEqualTo("Ivan");
        assertThat(saved.getGuestEmail()).isEqualTo("ivan@test.com");
        assertThat(saved.getCustomer()).isNull();
        assertThat(saved.getGuestAccessToken()).isNotNull();

        ArgumentCaptor<SupportMessageEntity> msgCaptor = ArgumentCaptor.forClass(SupportMessageEntity.class);
        verify(supportMessageRepository).save(msgCaptor.capture());
        assertThat(msgCaptor.getValue().getAuthorType()).isEqualTo(SupportMessageAuthorType.GUEST);
    }

    @Test
    void createConversation_asGuest_throwsWhenGuestEmailMissing() {
        when(securityContextHelper.getCurrentAccountPrincipal()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> supportService.createConversation(new CreateSupportConversationRequestDto(
                null,
                "Hello",
                "Ivan",
                null
        ))).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void addMessage_asAdmin_setsAuthorAdmin() {
        UUID adminId = UUID.randomUUID();
        UUID convId = UUID.randomUUID();

        AccountPrincipal principal = new AccountPrincipal(adminId, AccountRole.ADMIN);
        when(securityContextHelper.getCurrentAccountPrincipal()).thenReturn(Optional.of(principal));

        AccountEntity admin = new AccountEntity();
        admin.setId(adminId);
        when(accountRepository.findById(adminId)).thenReturn(Optional.of(admin));

        SupportConversationEntity conv = new SupportConversationEntity();
        conv.setId(convId);
        conv.setStatus(SupportConversationStatus.OPEN);
        when(supportConversationRepository.findById(convId)).thenReturn(Optional.of(conv));

        when(supportMessageRepository.findAllByConversation_IdOrderByCreatedAtAsc(convId)).thenReturn(List.of());
        when(supportMapper.toDetail(any(), any())).thenReturn(
                new SupportConversationDetailResponseDto(
                        convId, null, SupportConversationStatus.OPEN, null, null, null,
                        java.time.Instant.now(), java.time.Instant.now(), List.of()
                )
        );

        supportService.addMessage(convId, new AddSupportMessageRequestDto("Reply from admin"), null);

        ArgumentCaptor<SupportMessageEntity> captor = ArgumentCaptor.forClass(SupportMessageEntity.class);
        verify(supportMessageRepository).save(captor.capture());
        assertThat(captor.getValue().getAuthorType()).isEqualTo(SupportMessageAuthorType.ADMIN);
        assertThat(captor.getValue().getAuthorAccount()).isSameAs(admin);
    }

    @Test
    void updateStatus_requiresManager() {
        UUID customerId = UUID.randomUUID();
        when(securityContextHelper.getCurrentAccountPrincipal())
                .thenReturn(Optional.of(new AccountPrincipal(customerId, AccountRole.CUSTOMER)));

        assertThatThrownBy(() -> supportService.updateStatus(
                UUID.randomUUID(),
                new UpdateSupportConversationStatusRequestDto(SupportConversationStatus.CLOSED)
        )).isInstanceOf(ResponseStatusException.class);
    }
}
