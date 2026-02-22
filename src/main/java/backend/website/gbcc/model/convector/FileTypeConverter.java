package backend.website.gbcc.model.convector;

import backend.website.gbcc.model.FileType;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class FileTypeConverter
        implements AttributeConverter<FileType, String> {

    @Override
    public String convertToDatabaseColumn(FileType attribute) {

        if (attribute == null) {
            return null;
        }

        return attribute.getMimeType();

    }


    @Override
    public FileType convertToEntityAttribute(String dbData) {

        if (dbData == null) {
            return null;
        }

        return FileType.fromMimeType(dbData);

    }

}