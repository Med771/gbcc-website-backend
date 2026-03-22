package backend.website.gbcc.logic.promotion;

import backend.website.gbcc.helper.SecurityContextHelper;
import backend.website.gbcc.logic.promotion.dto.CreatePromotionRequestDto;
import backend.website.gbcc.logic.promotion.dto.PromotionResponseDto;
import backend.website.gbcc.model.PromotionScopeType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PromotionServiceImplTest {

    @Mock
    private PromotionRepository promotionRepository;

    @Mock
    private PromotionMapper promotionMapper;

    @Mock
    private SecurityContextHelper securityContextHelper;

    @InjectMocks
    private PromotionServiceImpl promotionService;

    @Test
    void create_shouldThrowForbidden_whenNotAdminOrOwner() {
        doThrow(new ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Only admin or owner can manage promotions"))
                .when(securityContextHelper).requireAdminOrOwner("Only admin or owner can manage promotions");

        CreatePromotionRequestDto dto = new CreatePromotionRequestDto(
                "Sale",
                new BigDecimal("10"),
                Instant.parse("2025-01-01T00:00:00Z"),
                Instant.parse("2025-12-31T23:59:59Z"),
                true,
                1,
                PromotionScopeType.ALL,
                null
        );

        assertThatThrownBy(() -> promotionService.create(dto))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode().value()).isEqualTo(403));
    }

    @Test
    void create_shouldPersist_whenAdminAndScopeAll() {
        PromotionEntity saved = new PromotionEntity();
        saved.setId(UUID.randomUUID());
        saved.setName("Sale");

        PromotionResponseDto response = new PromotionResponseDto(
                saved.getId(),
                "Sale",
                new BigDecimal("10"),
                Instant.parse("2025-01-01T00:00:00Z"),
                Instant.parse("2025-12-31T23:59:59Z"),
                true,
                1,
                PromotionScopeType.ALL,
                null,
                Instant.now(),
                Instant.now()
        );

        CreatePromotionRequestDto dto = new CreatePromotionRequestDto(
                "Sale",
                new BigDecimal("10"),
                Instant.parse("2025-01-01T00:00:00Z"),
                Instant.parse("2025-12-31T23:59:59Z"),
                true,
                1,
                PromotionScopeType.ALL,
                null
        );

        when(promotionRepository.save(any(PromotionEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(promotionMapper.toResponse(any(PromotionEntity.class))).thenReturn(response);

        PromotionResponseDto result = promotionService.create(dto);

        assertThat(result.name()).isEqualTo("Sale");
        verify(promotionRepository).save(any(PromotionEntity.class));
    }

    @Test
    void create_shouldThrowBadRequest_whenScopeAllButReferenceSet() {
        CreatePromotionRequestDto dto = new CreatePromotionRequestDto(
                "Sale",
                new BigDecimal("10"),
                null,
                null,
                true,
                1,
                PromotionScopeType.ALL,
                UUID.randomUUID()
        );

        assertThatThrownBy(() -> promotionService.create(dto))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getReason()).contains("scopeReferenceId"));
    }

    @Test
    void create_shouldThrowBadRequest_whenValidFromAfterValidTo() {
        CreatePromotionRequestDto dto = new CreatePromotionRequestDto(
                "Sale",
                new BigDecimal("10"),
                Instant.parse("2025-12-31T00:00:00Z"),
                Instant.parse("2025-01-01T00:00:00Z"),
                true,
                1,
                PromotionScopeType.ALL,
                null
        );

        assertThatThrownBy(() -> promotionService.create(dto))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getReason()).contains("validFrom"));
    }
}
