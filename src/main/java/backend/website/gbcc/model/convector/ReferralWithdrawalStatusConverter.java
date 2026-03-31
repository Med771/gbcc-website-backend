package backend.website.gbcc.model.convector;

import backend.website.gbcc.model.ReferralWithdrawalStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ReferralWithdrawalStatusConverter implements AttributeConverter<ReferralWithdrawalStatus, String> {

    @Override
    public String convertToDatabaseColumn(ReferralWithdrawalStatus attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public ReferralWithdrawalStatus convertToEntityAttribute(String dbData) {
        return dbData == null ? null : ReferralWithdrawalStatus.valueOf(dbData);
    }
}
