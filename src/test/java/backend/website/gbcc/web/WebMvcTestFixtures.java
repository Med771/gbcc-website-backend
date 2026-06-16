package backend.website.gbcc.web;

import backend.website.gbcc.logic.account.dto.AccountResponseDto;
import backend.website.gbcc.logic.contactrequest.dto.ContactRequestResponseDto;
import backend.website.gbcc.logic.cooperationrequest.dto.CooperationRequestResponseDto;
import backend.website.gbcc.logic.file.dto.FileUploadResponseDto;
import backend.website.gbcc.logic.news.dto.NewsResponseDto;
import backend.website.gbcc.logic.order.dto.OrderItemResponseDto;
import backend.website.gbcc.logic.order.dto.OrderResponseDto;
import backend.website.gbcc.logic.order.dto.OrderStatusHistoryResponseDto;
import backend.website.gbcc.logic.product.dto.ProductResponseDto;
import backend.website.gbcc.logic.promotion.dto.PromotionResponseDto;
import backend.website.gbcc.logic.support.dto.CreateSupportConversationResponseDto;
import backend.website.gbcc.logic.support.dto.SupportConversationDetailResponseDto;
import backend.website.gbcc.logic.support.dto.SupportConversationSummaryResponseDto;
import backend.website.gbcc.model.AccountPrincipal;
import backend.website.gbcc.model.AccountRegistrationStatus;
import backend.website.gbcc.model.AccountRole;
import backend.website.gbcc.model.NewsCategory;
import backend.website.gbcc.model.OrderPaymentMethod;
import backend.website.gbcc.model.OrderStatus;
import backend.website.gbcc.model.PromotionScopeType;
import backend.website.gbcc.model.SupportConversationStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class WebMvcTestFixtures {

    public static final UUID UUID_1 = UUID.fromString("00000000-0000-4000-8000-000000000001");
    public static final UUID UUID_2 = UUID.fromString("00000000-0000-4000-8000-000000000002");

    public static final AccountPrincipal PRINCIPAL_ADMIN = new AccountPrincipal(UUID_1, AccountRole.ADMIN);
    public static final AccountPrincipal PRINCIPAL_OWNER = new AccountPrincipal(UUID_1, AccountRole.OWNER);
    public static final AccountPrincipal PRINCIPAL_CUSTOMER = new AccountPrincipal(UUID_1, AccountRole.CUSTOMER);

    private static final Instant T0 = Instant.parse("2025-01-01T00:00:00Z");

    private WebMvcTestFixtures() {
    }

    public static AccountResponseDto sampleAccountResponse(AccountRole role) {
        return new AccountResponseDto(
                UUID_1,
                "A",
                "B",
                null,
                role == AccountRole.CUSTOMER ? null : "Admin",
                "+70000000000",
                "a@example.com",
                role,
                AccountRegistrationStatus.ACTIVE,
                false,
                null,
                null,
                T0,
                T0
        );
    }

    public static ProductResponseDto sampleProductResponse(UUID id) {
        return new ProductResponseDto(
                id,
                "class",
                "series",
                "brand",
                "desc",
                "tagline",
                "tags",
                0,
                "d",
                "l",
                0,
                100,
                100,
                100,
                new BigDecimal("1.000"),
                new BigDecimal("100.00"),
                new BigDecimal("100.00"),
                new BigDecimal("100.00"),
                null,
                null,
                BigDecimal.ZERO,
                new BigDecimal("100.00"),
                0,
                true,
                List.of(),
                T0,
                T0
        );
    }

    public static NewsResponseDto sampleNewsResponse(UUID id) {
        return new NewsResponseDto(
                id,
                "title",
                "preview",
                "content",
                null,
                NewsCategory.PRODUCTS,
                true,
                T0,
                UUID_1,
                UUID_1,
                T0,
                T0
        );
    }

    public static OrderResponseDto sampleOrderResponse(UUID id) {
        return new OrderResponseDto(
                id,
                1L,
                UUID_1,
                OrderStatus.CREATED,
                "Создан",
                "addr",
                null,
                OrderPaymentMethod.CARD_ONLINE,
                "Онлайн",
                null,
                null,
                null,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                null,
                null,
                null,
                null,
                null,
                null,
                List.<OrderItemResponseDto>of(),
                List.<OrderStatusHistoryResponseDto>of(),
                T0,
                T0
        );
    }

    public static PromotionResponseDto samplePromotionResponse(UUID id) {
        return new PromotionResponseDto(
                id,
                "promo",
                new BigDecimal("10.0"),
                T0,
                T0,
                true,
                1,
                PromotionScopeType.ALL,
                null,
                T0,
                T0
        );
    }

    public static ContactRequestResponseDto sampleContactRequestResponse(UUID id) {
        return new ContactRequestResponseDto(
                id,
                "n",
                "e@e.com",
                null,
                "msg",
                true,
                null,
                null,
                null,
                null,
                T0,
                T0
        );
    }

    public static CooperationRequestResponseDto sampleCooperationRequestResponse(UUID id) {
        return new CooperationRequestResponseDto(
                id,
                "n",
                "+1",
                "e@e.com",
                "type",
                "c",
                null,
                true,
                null,
                null,
                null,
                null,
                T0,
                T0
        );
    }

    public static SupportConversationSummaryResponseDto sampleSupportSummary(UUID id) {
        return new SupportConversationSummaryResponseDto(
                id,
                "subj",
                SupportConversationStatus.OPEN,
                "g",
                "g@g.com",
                null,
                T0,
                T0
        );
    }

    public static SupportConversationDetailResponseDto sampleSupportDetail(UUID id) {
        return new SupportConversationDetailResponseDto(
                id,
                "subj",
                SupportConversationStatus.OPEN,
                "g",
                "g@g.com",
                null,
                T0,
                T0,
                List.of()
        );
    }

    public static CreateSupportConversationResponseDto sampleCreateSupportConversationResponse(UUID id) {
        return new CreateSupportConversationResponseDto(
                id,
                "guest-token",
                "subj",
                SupportConversationStatus.OPEN,
                T0
        );
    }

    public static FileUploadResponseDto sampleFileUploadResponse(UUID key) {
        FileUploadResponseDto dto = new FileUploadResponseDto();
        dto.setFileId(key);
        dto.setKey(key.toString());
        dto.setBucket("default");
        dto.setFileName("f.bin");
        dto.setMimeType("application/octet-stream");
        dto.setSize(4L);
        return dto;
    }
}
