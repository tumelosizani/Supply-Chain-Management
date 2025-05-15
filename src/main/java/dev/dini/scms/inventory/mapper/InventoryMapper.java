package dev.dini.scms.inventory.mapper;

import dev.dini.scms.inventory.dto.InventoryRequestDTO;
import dev.dini.scms.inventory.dto.InventoryResponseDTO;
import dev.dini.scms.inventory.dto.InventorySummaryDTO;
import dev.dini.scms.inventory.entity.Inventory;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface InventoryMapper {

    @Mapping(source = "product.id", target = "productId")
    InventoryResponseDTO toResponseDTO(Inventory inventory);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Inventory toEntity(InventoryRequestDTO inventoryRequestDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(InventoryRequestDTO inventoryRequestDTO, @MappingTarget Inventory inventory);

    @Mapping(source = "product.id", target = "productId")
    InventorySummaryDTO toSummaryDTO(Inventory inventory);

}
