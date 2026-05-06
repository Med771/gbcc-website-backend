package backend.website.gbcc.logic.crm.task;

import backend.website.gbcc.config.OpenApiConstants;
import backend.website.gbcc.logic.crm.task.dto.CreateCrmTaskRequestDto;
import backend.website.gbcc.logic.crm.task.dto.CrmTaskResponseDto;
import backend.website.gbcc.logic.crm.task.dto.UpdateCrmTaskRequestDto;
import backend.website.gbcc.model.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/crm/tasks")
@RequiredArgsConstructor
@Validated
@Tag(name = "CRM — Tasks")
public class CrmTaskController {

    private final CrmTaskService taskService;

    @Operation(summary = "Создать задачу")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CrmTaskResponseDto create(@Valid @RequestBody CreateCrmTaskRequestDto dto) {
        return taskService.create(dto);
    }

    @Operation(summary = "Обновить задачу")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @PutMapping("/{id}")
    public CrmTaskResponseDto update(@PathVariable UUID id, @Valid @RequestBody UpdateCrmTaskRequestDto dto) {
        return taskService.update(id, dto);
    }

    @Operation(summary = "Удалить")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        taskService.delete(id);
    }

    @Operation(summary = "По id")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @GetMapping("/{id}")
    public CrmTaskResponseDto getById(@PathVariable UUID id) {
        return taskService.getById(id);
    }

    @Operation(summary = "Поиск")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @GetMapping
    public PageResponse<CrmTaskResponseDto> search(
            @RequestParam(required = false) UUID organizationId,
            @RequestParam(required = false) UUID leadId,
            @RequestParam(required = false) CrmTaskStatus status,
            @RequestParam(required = false) UUID assigneeId,
            @RequestParam(required = false) Instant dueAfter,
            @RequestParam(required = false) Instant dueBefore,
            @Parameter(hidden = true) Pageable pageable
    ) {
        return taskService.search(organizationId, leadId, status, assigneeId, dueAfter, dueBefore, pageable);
    }

    @Operation(summary = "Просроченные открытые задачи (в рамках скоупа)")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @GetMapping("/overdue")
    public List<CrmTaskResponseDto> overdue() {
        return taskService.overdue();
    }
}
