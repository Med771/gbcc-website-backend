package backend.website.gbcc.logic.account;

import backend.website.gbcc.logic.account.dto.AccountResponseDto;
import backend.website.gbcc.logic.account.dto.AccountSearchRequestDto;
import backend.website.gbcc.logic.account.dto.ActivateCustomerAccountRequestDto;
import backend.website.gbcc.logic.account.dto.CreateAdminAccountRequestDto;
import backend.website.gbcc.logic.account.dto.CreateGuestCustomerAccountRequestDto;
import backend.website.gbcc.logic.account.dto.UpdateCustomerAccountRequestDto;
import backend.website.gbcc.model.AccountRole;
import backend.website.gbcc.model.dto.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
@Validated
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/admin")
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponseDto createAdmin(@Valid @RequestBody CreateAdminAccountRequestDto requestDto) {
        return accountService.createAdmin(requestDto);
    }

    @PostMapping("/customer/guest")
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponseDto createGuestCustomer(@Valid @RequestBody CreateGuestCustomerAccountRequestDto requestDto) {
        return accountService.createGuestCustomer(requestDto);
    }

    @PutMapping("/customer/{accountId}")
    public AccountResponseDto updateCustomer(
            @PathVariable UUID accountId,
            @Valid @RequestBody UpdateCustomerAccountRequestDto requestDto
    ) {
        return accountService.updateCustomer(accountId, requestDto);
    }

    @PatchMapping("/customer/{accountId}/activate")
    public AccountResponseDto activateCustomer(
            @PathVariable UUID accountId,
            @Valid @RequestBody ActivateCustomerAccountRequestDto requestDto
    ) {
        return accountService.activateCustomer(accountId, requestDto);
    }

    @DeleteMapping("/customer/{accountId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCustomer(@PathVariable UUID accountId) {
        accountService.deleteCustomer(accountId);
    }

    @GetMapping("/{accountId}")
    public AccountResponseDto getById(@PathVariable UUID accountId) {
        return accountService.getById(accountId);
    }

    @GetMapping
    public PageResponse<AccountResponseDto> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) AccountRole role,
            @RequestParam(required = false) Boolean isBlocked,
            Pageable pageable
    ) {
        AccountSearchRequestDto requestDto = new AccountSearchRequestDto(name, phone, email, role, isBlocked);
        Page<AccountResponseDto> result = accountService.search(requestDto, pageable);
        return new PageResponse<>(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    @PatchMapping("/{accountId}/block")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void block(@PathVariable UUID accountId) {
        accountService.block(accountId);
    }

    @PatchMapping("/{accountId}/unblock")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unblock(@PathVariable UUID accountId) {
        accountService.unblock(accountId);
    }
}
