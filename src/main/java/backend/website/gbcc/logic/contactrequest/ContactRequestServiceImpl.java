package backend.website.gbcc.logic.contactrequest;

import backend.website.gbcc.helper.SecurityContextHelper;
import backend.website.gbcc.logic.account.AccountEntity;
import backend.website.gbcc.logic.account.AccountRepository;
import backend.website.gbcc.logic.contactrequest.dto.ContactRequestResponseDto;
import backend.website.gbcc.logic.contactrequest.dto.CreateContactRequestDto;
import backend.website.gbcc.model.AccountPrincipal;
import backend.website.gbcc.model.AccountRole;
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

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContactRequestServiceImpl implements ContactRequestService {

    private static final String FORBIDDEN_NOT_MANAGER = "Only admin or owner can access contact requests";

    private final ContactRequestRepository contactRequestRepository;
    private final AccountRepository accountRepository;
    private final ContactRequestMapper contactRequestMapper;
    private final SecurityContextHelper securityContextHelper;

    @Override
    @Transactional
    public ContactRequestResponseDto create(CreateContactRequestDto requestDto) {
        ContactRequestEntity entity = new ContactRequestEntity();
        entity.setName(normalizeRequired(requestDto.name(), "name"));
        entity.setEmail(normalizeEmailRequired(requestDto.email()));
        entity.setPhone(normalizeOptional(requestDto.phone()));
        entity.setMessage(normalizeRequired(requestDto.message(), "message"));
        entity.setConsentProcessing(Boolean.TRUE.equals(requestDto.consentProcessing()));

        ContactRequestEntity saved = contactRequestRepository.save(entity);
        return contactRequestMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ContactRequestResponseDto> searchForAdmin(Boolean onlyUnassigned, String query, Pageable pageable) {
        securityContextHelper.requireAdminOrOwner(FORBIDDEN_NOT_MANAGER);
        Specification<ContactRequestEntity> spec = Specification.allOf(
                ContactRequestSpecification.onlyUnassigned(onlyUnassigned),
                ContactRequestSpecification.queryLike(query)
        );
        Page<ContactRequestResponseDto> page = contactRequestRepository.findAll(spec, pageable)
                .map(contactRequestMapper::toResponse);
        return PageResponse.fromPage(page);
    }

    @Override
    @Transactional(readOnly = true)
    public ContactRequestResponseDto getByIdForAdmin(UUID id) {
        securityContextHelper.requireAdminOrOwner(FORBIDDEN_NOT_MANAGER);
        ContactRequestEntity entity = findOrThrow(id);
        return contactRequestMapper.toResponse(entity);
    }

    @Override
    @Transactional
    public ContactRequestResponseDto take(UUID id) {
        AccountPrincipal principal = securityContextHelper.requireAdminOrOwner(FORBIDDEN_NOT_MANAGER);
        ContactRequestEntity entity = findOrThrow(id);

        if (entity.getAssignedTo() != null) {
            if (entity.getAssignedTo().getId().equals(principal.accountId())) {
                return contactRequestMapper.toResponse(entity);
            }
            if (principal.role() != AccountRole.OWNER) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Request is already assigned to another administrator");
            }
        }

        AccountEntity admin = accountRepository.findById(principal.accountId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account not found"));

        entity.setAssignedTo(admin);
        entity.setAssignedAt(Instant.now());
        return contactRequestMapper.toResponse(contactRequestRepository.save(entity));
    }

    @Override
    @Transactional
    public ContactRequestResponseDto release(UUID id) {
        AccountPrincipal principal = securityContextHelper.requireAdminOrOwner(FORBIDDEN_NOT_MANAGER);
        ContactRequestEntity entity = findOrThrow(id);

        if (entity.getAssignedTo() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request is not assigned");
        }
        boolean isOwner = principal.role() == AccountRole.OWNER;
        if (!isOwner && !entity.getAssignedTo().getId().equals(principal.accountId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the assigned administrator or owner can release");
        }

        entity.setAssignedTo(null);
        entity.setAssignedAt(null);
        return contactRequestMapper.toResponse(contactRequestRepository.save(entity));
    }

    private ContactRequestEntity findOrThrow(UUID id) {
        return contactRequestRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact request not found"));
    }

    private String normalizeRequired(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " is required");
        }
        return value.trim();
    }

    private String normalizeEmailRequired(String email) {
        String normalized = normalizeRequired(email, "email").toLowerCase();
        if (normalized.length() > 255) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "email is too long");
        }
        return normalized;
    }

    private String normalizeOptional(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String t = value.trim();
        return t.length() > 64 ? t.substring(0, 64) : t;
    }
}
