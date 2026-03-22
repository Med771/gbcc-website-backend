package backend.website.gbcc.model.convector;

import backend.website.gbcc.model.AccountRole;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class AccountRoleConverter implements AttributeConverter<AccountRole, String> {

    @Override
    public String convertToDatabaseColumn(AccountRole attribute) {
        return attribute != null ? attribute.name() : null;
    }

    @Override
    public AccountRole convertToEntityAttribute(String dbData) {
        return dbData != null ? AccountRole.valueOf(dbData) : null;
    }
}
