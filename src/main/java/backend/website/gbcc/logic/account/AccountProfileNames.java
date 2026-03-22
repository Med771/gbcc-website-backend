package backend.website.gbcc.logic.account;

import org.springframework.util.StringUtils;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class AccountProfileNames {

    private AccountProfileNames() {
    }

    /**
     * Сохраняет агрегированное поле {@code name} для поиска и обратной совместимости: «фамилия имя отчество».
     */
    public static void syncLegacyNameField(AccountEntity account) {
        String line = buildDisplayLine(account.getLastName(), account.getFirstName(), account.getPatronymic());
        account.setName(StringUtils.hasText(line) ? line : null);
    }

    public static String buildDisplayLine(String lastName, String firstName, String patronymic) {
        return Stream.of(lastName, firstName, patronymic)
                .filter(StringUtils::hasText)
                .map(String::trim)
                .collect(Collectors.joining(" "));
    }
}
