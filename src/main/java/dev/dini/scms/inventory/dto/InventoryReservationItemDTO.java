package dev.dini.scms.inventory.dto;

/**
 * Minimal product-quantity pair used by the inventory module to check availability
 * and reserve stock without depending on order module DTOs.
 */
public record InventoryReservationItemDTO(Long productId, int quantity) {
}
