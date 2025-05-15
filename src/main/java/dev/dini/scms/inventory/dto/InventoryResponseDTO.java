package dev.dini.scms.inventory.dto;

import java.io.Serializable;
import java.time.LocalDateTime;


public record InventoryResponseDTO(
        Long id,
        Long productId,
        Integer quantity,
        String warehouseLocation,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) implements Serializable { }