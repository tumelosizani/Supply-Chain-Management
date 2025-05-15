package dev.dini.scms.product.mapper;

import dev.dini.scms.product.dto.*;
import dev.dini.scms.product.entity.Product;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "inventories", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    Product toEntity(ProductRequestDTO productResponseDTO);

    ProductResponseDTO toResponseDTO(Product product);

    ProductSummaryDTO toSummaryDTO(Product product);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "inventories", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void partialUpdate(ProductRequestDTO productRequestDTO, @MappingTarget Product product);
}

