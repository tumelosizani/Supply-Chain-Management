package dev.dini.scms.inventory.dto;

import java.io.Serializable;


public record InventoryResponseDTO(
        Long id,
        Long productId,
        Integer quantity,
        String warehouseLocation
) implements Serializable { }