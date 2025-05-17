package dev.dini.scms.procurement.mapper;

import dev.dini.scms.procurement.dto.*;
import dev.dini.scms.procurement.entity.PurchaseOrder;
import org.mapstruct.*;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {PurchaseOrderItemMapper.class}
)
public interface PurchaseOrderMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "supplier", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    PurchaseOrder toEntity(PurchaseOrderRequestDTO purchaseOrderRequestDTO);

    @Mapping(source = "supplier.id", target = "supplierId")
    PurchaseOrderResponseDTO toResponseDTO(PurchaseOrder purchaseOrder);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "supplier", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void partialUpdate(PurchaseOrderRequestDTO purchaseOrderRequestDTO, @MappingTarget PurchaseOrder purchaseOrder);
}