package backend.website.gbcc.logic.account;

import backend.website.gbcc.logic.account.dto.AccountResponseDto;
import backend.website.gbcc.logic.account.dto.AccountSearchRequestDto;
import backend.website.gbcc.logic.account.dto.CreateAdminAccountRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AccountService {
    AccountResponseDto createAdmin(CreateAdminAccountRequestDto requestDto);

    void ensureOwnerExists(String email, String rawPassword);

    AccountResponseDto getById(UUID accountId);

    Page<AccountResponseDto> search(AccountSearchRequestDto requestDto, Pageable pageable);

    void block(UUID accountId);

    void unblock(UUID accountId);
}
