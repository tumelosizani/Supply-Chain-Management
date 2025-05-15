package dev.dini.scms.procurement.mapper;

import dev.dini.scms.procurement.dto.*;
import dev.dini.scms.procurement.entity.PurchaseOrderItem;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PurchaseOrderItemMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "purchaseOrder", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    PurchaseOrderItem toEntity(PurchaseOrderItemRequestDTO orderItemRequestDTO);

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "purchaseOrder.id", target = "purchaseOrderId")
    PurchaseOrderItemResponseDTO toResponseDTO(PurchaseOrderItem purchaseOrderItem);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "purchaseOrder", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void partialUpdate(PurchaseOrderItemRequestDTO orderItemRequestDTO, @MappingTarget PurchaseOrderItem purchaseOrderItem);
}
