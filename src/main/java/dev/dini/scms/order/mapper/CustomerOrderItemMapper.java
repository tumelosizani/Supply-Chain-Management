package dev.dini.scms.order.mapper;


import dev.dini.scms.order.dto.CustomerOrderItemRequestDTO;
import dev.dini.scms.order.dto.CustomerOrderItemResponseDTO;
import dev.dini.scms.order.entity.CustomerOrderItem;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CustomerOrderItemMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customerOrder", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    CustomerOrderItem toEntity(CustomerOrderItemRequestDTO orderItemRequestDTO);

    @Mapping( source = "product.id", target = "productId")
    @Mapping(source = "customerOrder.id", target = "customerOrderId")
    CustomerOrderItemResponseDTO toResponseDTO(CustomerOrderItem customerOrderItem);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customerOrder", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void partialUpdate(CustomerOrderItemRequestDTO orderItemRequestDTO, @MappingTarget CustomerOrderItem customerOrderItem);
}
