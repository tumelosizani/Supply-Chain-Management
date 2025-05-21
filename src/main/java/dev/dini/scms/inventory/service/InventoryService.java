package dev.dini.scms.inventory.service;

import dev.dini.scms.inventory.dto.*;
import dev.dini.scms.inventory.entity.Inventory;

import java.util.List;

public interface InventoryService {
    InventoryResponseDTO createInventory(InventoryRequestDTO inventoryRequestDTO);

    InventoryResponseDTO updateInventory(Long id, InventoryRequestDTO inventoryRequestDTO);

    void deleteInventory(Long id);


    List<InventorySummaryDTO> getAllInventorySummaries();

    /**
     * Checks if there are enough inventories for a given product.
     *
     * @param productId the ID of the product
     * @param quantity the quantity to check
     * @return true if sufficient inventory exists, false otherwise
     */
    boolean isInventoryAvailable(Long productId, int quantity);


    /**
     * Reduces the quantity of inventory for a given product.
     * This method is kept for backward compatibility but should be replaced with the reservation system.
     *
     * @param productId the ID of the product
     * @param quantity the quantity to reduce
     * @deprecated Use reserveInventory and confirmReservation instead
     */
    void reduceInventoryQuantity(Long productId, int quantity);

    
    /**
     * Saves the given inventory entity and returns its response DTO.
     *
     * @param inventory the inventory entity to save
     */
    void saveInventory(Inventory inventory);

    int getStockLevel(Long productId);

    InventoryResponseDTO addStock(StockUpdateRequestDTO request);
}
