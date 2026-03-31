package backend.website.gbcc.logic.referral;

import backend.website.gbcc.config.OpenApiConstants;
import backend.website.gbcc.logic.referral.dto.ReferralApplyInviteCodeRequestDto;
import backend.website.gbcc.logic.referral.dto.ReferralClickAdminDto;
import backend.website.gbcc.logic.referral.dto.ReferralClickRequestDto;
import backend.website.gbcc.logic.referral.dto.ReferralCommissionAdminDto;
import backend.website.gbcc.logic.referral.dto.ReferralMeResponseDto;
import backend.website.gbcc.logic.referral.dto.ReferralRefereeRowDto;
import backend.website.gbcc.logic.referral.dto.ReferralRegistrationAdminDto;
import backend.website.gbcc.logic.referral.dto.ReferralUpdateMyCodeRequestDto;
import backend.website.gbcc.logic.referral.dto.ReferralWithdrawRequestDto;
import backend.website.gbcc.logic.referral.dto.ReferralWithdrawalAdminDto;
import backend.website.gbcc.logic.referral.dto.ReferralWithdrawalCreatedResponseDto;
import backend.website.gbcc.logic.referral.dto.ReferralWithdrawalRejectRequestDto;
import backend.website.gbcc.model.dto.PageResponse;
import backend.website.gbcc.model.error.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/referral")
@RequiredArgsConstructor
@Validated
@Tag(name = "Referrals", description = """
        Реферальная программа: клики по ссылке, приглашение по коду (один раз), 5%% от суммы заказа реферала при статусе DELIVERED, \
        вывод средств от минимальной суммы (настраивается). Админы видят полную активность и обрабатывают заявки на вывод.
        """)
public class ReferralController {

    private final ReferralService referralService;

    @Operation(summary = "Зафиксировать переход по реферальной ссылке", description = "Публично. Увеличивает счётчик «перешли по ссылке» для владельца кода.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Учтено"),
            @ApiResponse(responseCode = "400", description = "Неверный формат кода",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Код не найден",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/clicks")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void recordClick(@Valid @RequestBody ReferralClickRequestDto requestDto) {
        referralService.recordClick(requestDto);
    }

    @Operation(summary = "Моя реферальная программа", description = "JWT. Только CUSTOMER: код, ссылка, статистика, баланс к выводу.")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Данные для личного кабинета",
                    content = @Content(schema = @Schema(implementation = ReferralMeResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Не клиент",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/me")
    public ReferralMeResponseDto getMyReferralDashboard() {
        return referralService.getMyReferralDashboard();
    }

    @Operation(summary = "Мои рефералы (таблица)", description = "JWT. Клиенты, зарегистрировавшиеся по вашему коду: маскированный email, сумма бонусов, число покупок.")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @GetMapping("/me/referees")
    public PageResponse<ReferralRefereeRowDto> getMyReferees(@Parameter(hidden = true) Pageable pageable) {
        Page<ReferralRefereeRowDto> page = referralService.getMyReferees(pageable);
        return PageResponse.fromPage(page);
    }

    @Operation(summary = "Ввести код приглашения", description = "JWT. Один раз за всё время. Нельзя указать свой код.")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Код применён"),
            @ApiResponse(responseCode = "400", description = "Неверный код",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Код уже был введён",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/me/invite-code")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void applyInviteCode(@Valid @RequestBody ReferralApplyInviteCodeRequestDto requestDto) {
        referralService.applyInviteCode(requestDto);
    }

    @Operation(summary = "Изменить свой реферальный код", description = "JWT. Формат XXX-XXXX (латиница и цифры), уникальность в системе.")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @PatchMapping("/me/code")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateMyReferralCode(@Valid @RequestBody ReferralUpdateMyCodeRequestDto requestDto) {
        referralService.updateMyReferralCode(requestDto);
    }

    @Operation(summary = "Заявка на вывод бонусов", description = "JWT. Сумма не ниже минимума из настроек и не больше доступного баланса (с учётом ожидающих заявок).")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Заявка создана",
                    content = @Content(schema = @Schema(implementation = ReferralWithdrawalCreatedResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Сумма или баланс",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/me/withdrawals")
    @ResponseStatus(HttpStatus.CREATED)
    public ReferralWithdrawalCreatedResponseDto requestWithdrawal(@Valid @RequestBody ReferralWithdrawRequestDto requestDto) {
        return referralService.requestWithdrawal(requestDto);
    }

    @Operation(summary = "Админ: все клики по реферальным ссылкам", description = "JWT. ADMIN/OWNER.")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @GetMapping("/admin/clicks")
    public PageResponse<ReferralClickAdminDto> listClicksForAdmin(@Parameter(hidden = true) Pageable pageable) {
        return PageResponse.fromPage(referralService.listClicksForAdmin(pageable));
    }

    @Operation(summary = "Админ: начисления (покупки рефералов)", description = "JWT. ADMIN/OWNER.")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @GetMapping("/admin/commissions")
    public PageResponse<ReferralCommissionAdminDto> listCommissionsForAdmin(@Parameter(hidden = true) Pageable pageable) {
        return PageResponse.fromPage(referralService.listCommissionsForAdmin(pageable));
    }

    @Operation(summary = "Админ: активации кода при регистрации", description = "JWT. Учётные записи с привязанным пригласителем.")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @GetMapping("/admin/registrations")
    public PageResponse<ReferralRegistrationAdminDto> listRegistrationsForAdmin(@Parameter(hidden = true) Pageable pageable) {
        return PageResponse.fromPage(referralService.listRegistrationsWithInviterForAdmin(pageable));
    }

    @Operation(summary = "Админ: заявки на вывод", description = "JWT. ADMIN/OWNER.")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @GetMapping("/admin/withdrawals")
    public PageResponse<ReferralWithdrawalAdminDto> listWithdrawalsForAdmin(@Parameter(hidden = true) Pageable pageable) {
        return PageResponse.fromPage(referralService.listWithdrawalsForAdmin(pageable));
    }

    @Operation(summary = "Админ: отметить вывод выполненным", description = "JWT. Только для заявки в статусе PENDING.")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @PostMapping("/admin/withdrawals/{withdrawalId}/pay")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markWithdrawalPaid(@PathVariable UUID withdrawalId) {
        referralService.markWithdrawalPaid(withdrawalId);
    }

    @Operation(summary = "Админ: отклонить заявку на вывод", description = "JWT. Комментарий опционален.")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @PostMapping(value = "/admin/withdrawals/{withdrawalId}/reject", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void rejectWithdrawal(
            @PathVariable UUID withdrawalId,
            @RequestBody(required = false) ReferralWithdrawalRejectRequestDto requestDto
    ) {
        referralService.rejectWithdrawal(withdrawalId, requestDto);
    }
}
