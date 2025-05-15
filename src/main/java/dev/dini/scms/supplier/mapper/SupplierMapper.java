package dev.dini.scms.supplier.mapper;

import dev.dini.scms.supplier.dto.*;
import dev.dini.scms.supplier.entity.Supplier;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SupplierMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "purchaseOrders", ignore = true)
    Supplier toEntity(SupplierRequestDTO requestDTO);

    SupplierResponseDTO toResponseDTO(Supplier supplier);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "purchaseOrders", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void partialUpdate(SupplierRequestDTO requestDTO, @MappingTarget Supplier supplier);
}