package dev.dini.scms.order.mapper;

import dev.dini.scms.order.dto.*;
import dev.dini.scms.order.entity.CustomerOrder;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {CustomerOrderItemMapper.class})
public interface CustomerOrderMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "shipments", ignore = true)
    CustomerOrder toEntity(CustomerOrderRequestDTO purchaseOrderRequestDTO);

    CustomerOrderResponseDTO toResponseDTO(CustomerOrder customerOrder);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "shipments", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void partialUpdate(CustomerOrderRequestDTO purchaseOrderRequestDTO, @MappingTarget CustomerOrder customerOrder);
}
