package dev.dini.scms.inventory.dto;

public record StockUpdateRequestDTO(
        Long productId,
        int quantity
) {
}
