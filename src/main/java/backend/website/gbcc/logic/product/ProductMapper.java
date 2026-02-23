package backend.website.gbcc.logic.product;

import backend.website.gbcc.logic.product.dto.CreateProductRequestDto;
import backend.website.gbcc.logic.product.dto.ProductResponseDto;
import backend.website.gbcc.logic.product.productclass.ProductClassEntity;
import backend.website.gbcc.logic.product.productseries.ProductSeriesEntity;
import backend.website.gbcc.logic.product.producttype.ProductTypeEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "productClass", source = "productClass")
    @Mapping(target = "productSeries", source = "productSeries")
    @Mapping(target = "productType", source = "productType")
    @Mapping(target = "brand", source = "request.brand")
    @Mapping(target = "description", source = "request.description")
    @Mapping(target = "heightMm", source = "request.heightMm")
    @Mapping(target = "widthMm", source = "request.widthMm")
    @Mapping(target = "lengthMm", source = "request.lengthMm")
    @Mapping(target = "weightKg", source = "request.weightKg")
    @Mapping(target = "price", source = "request.price")
    @Mapping(target = "isActive", expression = "java(resolveIsActive(request))")
    ProductEntity toEntity(
            CreateProductRequestDto request,
            ProductClassEntity productClass,
            ProductSeriesEntity productSeries,
            ProductTypeEntity productType
    );

    @Mapping(target = "id", source = "entity.id")
    @Mapping(target = "className", expression = "java(entity.getProductClass().getName())")
    @Mapping(target = "seriesName", expression = "java(entity.getProductSeries() != null ? entity.getProductSeries().getName() : null)")
    @Mapping(target = "typeName", expression = "java(entity.getProductType() != null ? entity.getProductType().getName() : null)")
    @Mapping(target = "photoFileIds", source = "photoFileIds")
    @Mapping(target = "createdAt", source = "entity.createdAt")
    @Mapping(target = "updatedAt", source = "entity.updatedAt")
    ProductResponseDto toResponse(ProductEntity entity, List<UUID> photoFileIds);

    default Boolean resolveIsActive(CreateProductRequestDto request) {
        return request.isActive() != null ? request.isActive() : Boolean.TRUE;
    }

    default String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
