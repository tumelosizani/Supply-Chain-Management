package dev.dini.scms.inventory.dto;

public record InventorySummaryDTO(
        Long id,
        Long productId,
        Integer quantity
) {}
