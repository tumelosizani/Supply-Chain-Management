package dev.dini.scms.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record InventoryRequestDTO(
        Long productId,
        @Min(0) int quantity,
        @NotBlank String warehouseLocation
) {}
