package dev.dini.scms.inventory.service;

import dev.dini.scms.inventory.entity.Inventory;
import org.springframework.stereotype.Service;

@Service
public class InventoryCalculationService {

    public Integer getAvailableQuantity(Inventory inventory) {
        if (inventory == null) {
            return null; // Or throw an IllegalArgumentException
        }
        return inventory.getQuantity() - inventory.getQuantityReserved();
    }
}