package backend.website.gbcc.logic.support;

import backend.website.gbcc.helper.SecurityContextHelper;
import backend.website.gbcc.logic.account.AccountEntity;
import backend.website.gbcc.logic.account.AccountRepository;
import backend.website.gbcc.logic.support.dto.AddSupportMessageRequestDto;
import backend.website.gbcc.logic.support.dto.CreateSupportConversationRequestDto;
import backend.website.gbcc.logic.support.dto.CreateSupportConversationResponseDto;
import backend.website.gbcc.logic.support.dto.SupportConversationDetailResponseDto;
import backend.website.gbcc.logic.support.dto.SupportConversationSummaryResponseDto;
import backend.website.gbcc.logic.support.dto.SupportSearchRequestDto;
import backend.website.gbcc.logic.support.dto.UpdateSupportConversationStatusRequestDto;
import backend.website.gbcc.model.AccountPrincipal;
import backend.website.gbcc.model.AccountRole;
import backend.website.gbcc.model.SupportConversationStatus;
import backend.website.gbcc.model.SupportMessageAuthorType;
import backend.website.gbcc.model.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SupportServiceImpl implements SupportService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final SupportConversationRepository supportConversationRepository;
    private final SupportMessageRepository supportMessageRepository;
    private final AccountRepository accountRepository;
    private final SecurityContextHelper securityContextHelper;
    private final SupportMapper supportMapper;

    @Override
    @Transactional
    public CreateSupportConversationResponseDto createConversation(CreateSupportConversationRequestDto requestDto) {
        String body = normalizeRequired(requestDto.message(), "message");

        Optional<AccountPrincipal> principalOpt = securityContextHelper.getCurrentAccountPrincipal();

        SupportConversationEntity conversation = new SupportConversationEntity();
        conversation.setStatus(SupportConversationStatus.OPEN);
        conversation.setSubject(normalizeOptionalSubject(requestDto.subject()));

        SupportMessageEntity firstMessage = new SupportMessageEntity();
        firstMessage.setBody(body);

        if (principalOpt.isEmpty()) {
            String guestName = normalizeRequired(requestDto.guestName(), "guestName");
            String guestEmail = normalizeEmailRequired(requestDto.guestEmail());
            conversation.setGuestName(guestName);
            conversation.setGuestEmail(guestEmail);
            conversation.setGuestAccessToken(generateGuestAccessToken());
            firstMessage.setAuthorType(SupportMessageAuthorType.GUEST);
            firstMessage.setAuthorAccount(null);
        } else {
            AccountPrincipal principal = principalOpt.get();
            if (principal.role() == AccountRole.ADMIN || principal.role() == AccountRole.OWNER) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Administrators cannot open a new support conversation");
            }
            if (principal.role() != AccountRole.CUSTOMER) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only customers can open a conversation when authenticated");
            }
            AccountEntity customer = accountRepository.findById(principal.accountId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account not found"));
            conversation.setCustomer(customer);
            firstMessage.setAuthorType(SupportMessageAuthorType.CUSTOMER);
            firstMessage.setAuthorAccount(customer);
        }

        SupportConversationEntity saved = supportConversationRepository.save(conversation);
        firstMessage.setConversation(saved);
        supportMessageRepository.save(firstMessage);

        String token = saved.getGuestAccessToken();
        return new CreateSupportConversationResponseDto(
                saved.getId(),
                token,
                saved.getSubject(),
                saved.getStatus(),
                saved.getCreatedAt()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public SupportConversationDetailResponseDto getConversation(UUID conversationId, String supportToken) {
        SupportConversationEntity conversation = findConversationOrThrow(conversationId);
        assertCanRead(conversation, supportToken);
        return buildDetail(conversation);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SupportConversationSummaryResponseDto> searchForAdmin(SupportSearchRequestDto filter, Pageable pageable) {
        securityContextHelper.requireAdminOrOwner("Only administrators can manage support conversations");
        Specification<SupportConversationEntity> spec = SupportSpecification.byFilter(filter);
        Page<SupportConversationSummaryResponseDto> page = supportConversationRepository.findAll(spec, pageable)
                .map(supportMapper::toSummary);
        return PageResponse.fromPage(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SupportConversationSummaryResponseDto> listMine(Pageable pageable) {
        UUID accountId = securityContextHelper.getCurrentAccountIdOrThrow();
        AccountEntity account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account not found"));
        if (account.getRole() != AccountRole.CUSTOMER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only customers can list own conversations");
        }
        Page<SupportConversationSummaryResponseDto> page = supportConversationRepository
                .findAllByCustomer_Id(accountId, pageable)
                .map(supportMapper::toSummary);
        return PageResponse.fromPage(page);
    }

    @Override
    @Transactional
    public SupportConversationDetailResponseDto addMessage(
            UUID conversationId,
            AddSupportMessageRequestDto requestDto,
            String supportToken
    ) {
        SupportConversationEntity conversation = findConversationOrThrow(conversationId);
        assertCanWrite(conversation, supportToken);
        if (conversation.getStatus() == SupportConversationStatus.CLOSED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Conversation is closed");
        }

        String body = normalizeRequired(requestDto.message(), "message");
        SupportMessageEntity message = new SupportMessageEntity();
        message.setConversation(conversation);
        message.setBody(body);

        if (isGuestTokenValid(conversation, supportToken)) {
            message.setAuthorType(SupportMessageAuthorType.GUEST);
            message.setAuthorAccount(null);
            supportMessageRepository.save(message);
            return buildDetail(conversation);
        }

        Optional<AccountPrincipal> principalOpt = securityContextHelper.getCurrentAccountPrincipal();

        if (principalOpt.isPresent()) {
            AccountPrincipal principal = principalOpt.get();
            if (isManager(principal)) {
                AccountEntity account = accountRepository.findById(principal.accountId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account not found"));
                message.setAuthorType(SupportMessageAuthorType.ADMIN);
                message.setAuthorAccount(account);
            } else if (principal.role() == AccountRole.CUSTOMER) {
                assertCustomerOwnsConversation(conversation, principal.accountId());
                AccountEntity customer = accountRepository.findById(principal.accountId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account not found"));
                message.setAuthorType(SupportMessageAuthorType.CUSTOMER);
                message.setAuthorAccount(customer);
            } else {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot send message");
            }
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Conversation access denied");
        }

        supportMessageRepository.save(message);
        return buildDetail(conversation);
    }

    @Override
    @Transactional
    public SupportConversationDetailResponseDto updateStatus(
            UUID conversationId,
            UpdateSupportConversationStatusRequestDto requestDto
    ) {
        securityContextHelper.requireAdminOrOwner("Only administrators can manage support conversations");
        SupportConversationEntity conversation = findConversationOrThrow(conversationId);
        conversation.setStatus(requestDto.status());
        supportConversationRepository.save(conversation);
        return buildDetail(conversation);
    }

    private SupportConversationDetailResponseDto buildDetail(SupportConversationEntity conversation) {
        List<SupportMessageEntity> messages = supportMessageRepository
                .findAllByConversation_IdOrderByCreatedAtAsc(conversation.getId());
        return supportMapper.toDetail(conversation, messages);
    }

    private SupportConversationEntity findConversationOrThrow(UUID conversationId) {
        return supportConversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));
    }

    private void assertCanRead(SupportConversationEntity conversation, String supportToken) {
        if (isGuestTokenValid(conversation, supportToken)) {
            return;
        }
        Optional<AccountPrincipal> principalOpt = securityContextHelper.getCurrentAccountPrincipal();
        if (principalOpt.isPresent() && isManager(principalOpt.get())) {
            return;
        }
        if (principalOpt.isPresent() && principalOpt.get().role() == AccountRole.CUSTOMER
                && conversation.getCustomer() != null
                && conversation.getCustomer().getId().equals(principalOpt.get().accountId())) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Conversation access denied");
    }

    private void assertCanWrite(SupportConversationEntity conversation, String supportToken) {
        if (isGuestTokenValid(conversation, supportToken)) {
            return;
        }
        Optional<AccountPrincipal> principalOpt = securityContextHelper.getCurrentAccountPrincipal();
        if (principalOpt.isPresent() && isManager(principalOpt.get())) {
            return;
        }
        if (principalOpt.isPresent() && principalOpt.get().role() == AccountRole.CUSTOMER) {
            assertCustomerOwnsConversation(conversation, principalOpt.get().accountId());
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Conversation access denied");
    }

    private boolean isGuestTokenValid(SupportConversationEntity conversation, String supportToken) {
        if (!StringUtils.hasText(supportToken) || conversation.getGuestAccessToken() == null) {
            return false;
        }
        return conversation.getGuestAccessToken().equals(supportToken.trim());
    }

    private void assertCustomerOwnsConversation(SupportConversationEntity conversation, UUID customerId) {
        if (conversation.getCustomer() == null || !conversation.getCustomer().getId().equals(customerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Conversation does not belong to current account");
        }
    }

    private boolean isManager(AccountPrincipal principal) {
        return principal.isAdminOrOwner();
    }

    private String normalizeRequired(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " is required");
        }
        return value.trim();
    }

    private String normalizeOptionalSubject(String subject) {
        if (!StringUtils.hasText(subject)) {
            return null;
        }
        String trimmed = subject.trim();
        return trimmed.length() > 500 ? trimmed.substring(0, 500) : trimmed;
    }

    private String normalizeEmailRequired(String email) {
        if (!StringUtils.hasText(email)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "guestEmail is required");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String generateGuestAccessToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

}
