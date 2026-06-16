package backend.website.gbcc.logic.managercommission;

import backend.website.gbcc.config.OpenApiConstants;
import backend.website.gbcc.logic.managercommission.dto.ManagerCommissionAdminDto;
import backend.website.gbcc.model.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/crm/manager-commissions")
@RequiredArgsConstructor
@Tag(name = "CRM Manager Commissions", description = "Комиссии менеджеров-привлечёнцев за доставленные заказы.")
public class ManagerCommissionController {

    private final ManagerCommissionService managerCommissionService;

    @Operation(summary = "Список комиссий менеджеров", description = "JWT. ADMIN/OWNER. Фильтр managerId опционален.")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @GetMapping
    public PageResponse<ManagerCommissionAdminDto> list(
            @RequestParam(required = false) UUID managerId,
            @Parameter(hidden = true) Pageable pageable
    ) {
        return PageResponse.fromPage(managerCommissionService.listForAdmin(managerId, pageable));
    }
}
