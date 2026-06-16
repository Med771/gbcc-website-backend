package backend.website.gbcc.model.convector;

import backend.website.gbcc.model.ReferralClientType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ReferralClientTypeConverter implements AttributeConverter<ReferralClientType, String> {

    @Override
    public String convertToDatabaseColumn(ReferralClientType attribute) {
        return attribute != null ? attribute.name() : null;
    }

    @Override
    public ReferralClientType convertToEntityAttribute(String dbData) {
        return dbData != null ? ReferralClientType.valueOf(dbData) : null;
    }
}
