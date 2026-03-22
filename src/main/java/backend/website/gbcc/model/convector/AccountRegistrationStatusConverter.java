package backend.website.gbcc.model.convector;

import backend.website.gbcc.model.AccountRegistrationStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class AccountRegistrationStatusConverter implements AttributeConverter<AccountRegistrationStatus, String> {

    @Override
    public String convertToDatabaseColumn(AccountRegistrationStatus attribute) {
        return attribute != null ? attribute.name() : null;
    }

    @Override
    public AccountRegistrationStatus convertToEntityAttribute(String dbData) {
        return dbData != null ? AccountRegistrationStatus.valueOf(dbData) : null;
    }
}
