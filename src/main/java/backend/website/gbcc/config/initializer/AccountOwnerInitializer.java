package backend.website.gbcc.config.initializer;

import backend.website.gbcc.config.property.AccountProperty;
import backend.website.gbcc.logic.account.AccountService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class AccountOwnerInitializer implements ApplicationRunner {

    private final AccountService accountService;
    private final AccountProperty accountProperty;

    @Override
    public void run(@NonNull ApplicationArguments args) {
        try {
            accountService.ensureOwnerExists(
                    accountProperty.getOwner().getEmail(),
                    accountProperty.getOwner().getPassword()
            );
        } catch (ResponseStatusException ex) {
            throw new IllegalStateException("Cannot initialize owner account from configuration", ex);
        }
    }
}
