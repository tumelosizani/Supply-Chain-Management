package dev.dini.scms.shipment.mapper;

import dev.dini.scms.shipment.dto.ShipmentRequestDTO;
import dev.dini.scms.shipment.dto.ShipmentResponseDTO;
import dev.dini.scms.shipment.entity.Shipment;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ShipmentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customerOrder", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Shipment toEntity(ShipmentRequestDTO requestDTO);

    @Mapping(source = "customerOrder.id", target = "orderId")
    ShipmentResponseDTO toResponseDTO(Shipment shipment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customerOrder", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void partialUpdate(ShipmentRequestDTO requestDTO, @MappingTarget Shipment shipment);
}