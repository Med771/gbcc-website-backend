package backend.website.gbcc.logic.cooperationrequest;

import backend.website.gbcc.helper.SecurityContextHelper;
import backend.website.gbcc.logic.account.AccountEntity;
import backend.website.gbcc.logic.account.AccountRepository;
import backend.website.gbcc.logic.cooperationrequest.dto.CooperationRequestResponseDto;
import backend.website.gbcc.logic.cooperationrequest.dto.CreateCooperationRequestDto;
import backend.website.gbcc.logic.file.FileEntity;
import backend.website.gbcc.logic.file.FileRepository;
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
public class CooperationRequestServiceImpl implements CooperationRequestService {

    private final CooperationRequestRepository cooperationRequestRepository;
    private final AccountRepository accountRepository;
    private final FileRepository fileRepository;
    private final CooperationRequestMapper cooperationRequestMapper;
    private final SecurityContextHelper securityContextHelper;

    private static final String FORBIDDEN_NOT_MANAGER = "Only admin or owner can access cooperation requests";

    @Override
    @Transactional
    public CooperationRequestResponseDto create(CreateCooperationRequestDto requestDto) {
        CooperationRequestEntity entity = new CooperationRequestEntity();
        entity.setName(normalizeRequired(requestDto.name(), "name"));
        entity.setPhone(normalizePhoneRequired(requestDto.phone()));
        entity.setEmail(normalizeEmailRequired(requestDto.email()));
        entity.setCooperationType(normalizeOptionalShort(requestDto.cooperationType(), 128));
        entity.setComment(normalizeOptionalComment(requestDto.comment()));
        entity.setConsentProcessing(Boolean.TRUE.equals(requestDto.consentProcessing()));

        if (requestDto.attachmentFileId() != null) {
            FileEntity file = fileRepository.findById(requestDto.attachmentFileId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "attachment file not found"));
            entity.setAttachmentFile(file);
        }

        CooperationRequestEntity saved = cooperationRequestRepository.save(entity);
        return cooperationRequestMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CooperationRequestResponseDto> searchForAdmin(Boolean onlyUnassigned, String query, Pageable pageable) {
        securityContextHelper.requireAdminOrOwner(FORBIDDEN_NOT_MANAGER);
        Specification<CooperationRequestEntity> spec = Specification.allOf(
                CooperationRequestSpecification.onlyUnassigned(onlyUnassigned),
                CooperationRequestSpecification.queryLike(query)
        );
        Page<CooperationRequestResponseDto> page = cooperationRequestRepository.findAll(spec, pageable)
                .map(cooperationRequestMapper::toResponse);
        return PageResponse.fromPage(page);
    }

    @Override
    @Transactional(readOnly = true)
    public CooperationRequestResponseDto getByIdForAdmin(UUID id) {
        securityContextHelper.requireAdminOrOwner(FORBIDDEN_NOT_MANAGER);
        CooperationRequestEntity entity = findOrThrow(id);
        return cooperationRequestMapper.toResponse(entity);
    }

    @Override
    @Transactional
    public CooperationRequestResponseDto take(UUID id) {
        AccountPrincipal principal = securityContextHelper.requireAdminOrOwner(FORBIDDEN_NOT_MANAGER);
        CooperationRequestEntity entity = findOrThrow(id);

        if (entity.getAssignedTo() != null) {
            if (entity.getAssignedTo().getId().equals(principal.accountId())) {
                return cooperationRequestMapper.toResponse(entity);
            }
            if (principal.role() != AccountRole.OWNER) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Request is already assigned to another administrator");
            }
        }

        AccountEntity admin = accountRepository.findById(principal.accountId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account not found"));

        entity.setAssignedTo(admin);
        entity.setAssignedAt(Instant.now());
        return cooperationRequestMapper.toResponse(cooperationRequestRepository.save(entity));
    }

    @Override
    @Transactional
    public CooperationRequestResponseDto release(UUID id) {
        AccountPrincipal principal = securityContextHelper.requireAdminOrOwner(FORBIDDEN_NOT_MANAGER);
        CooperationRequestEntity entity = findOrThrow(id);

        if (entity.getAssignedTo() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request is not assigned");
        }
        boolean isOwner = principal.role() == AccountRole.OWNER;
        if (!isOwner && !entity.getAssignedTo().getId().equals(principal.accountId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the assigned administrator or owner can release");
        }

        entity.setAssignedTo(null);
        entity.setAssignedAt(null);
        return cooperationRequestMapper.toResponse(cooperationRequestRepository.save(entity));
    }

    private CooperationRequestEntity findOrThrow(UUID id) {
        return cooperationRequestRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cooperation request not found"));
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

    private String normalizePhoneRequired(String phone) {
        if (!StringUtils.hasText(phone)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "phone is required");
        }
        String t = phone.trim();
        if (t.length() > 64) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "phone is too long");
        }
        return t;
    }

    private String normalizeOptionalShort(String value, int maxLen) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String t = value.trim();
        return t.length() > maxLen ? t.substring(0, maxLen) : t;
    }

    private String normalizeOptionalComment(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
