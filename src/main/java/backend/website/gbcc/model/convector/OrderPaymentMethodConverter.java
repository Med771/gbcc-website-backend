package backend.website.gbcc.model.convector;

import backend.website.gbcc.model.OrderPaymentMethod;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class OrderPaymentMethodConverter implements AttributeConverter<OrderPaymentMethod, String> {

    @Override
    public String convertToDatabaseColumn(OrderPaymentMethod attribute) {
        return attribute != null ? attribute.name() : null;
    }

    @Override
    public OrderPaymentMethod convertToEntityAttribute(String dbData) {
        return dbData != null ? OrderPaymentMethod.valueOf(dbData) : null;
    }
}
