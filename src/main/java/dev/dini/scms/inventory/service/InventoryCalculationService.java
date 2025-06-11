package dev.dini.scms.inventory.service;

import dev.dini.scms.inventory.entity.Inventory;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class InventoryCalculationService {

    public Integer getAvailableQuantity(Inventory inventory) {
        Objects.requireNonNull(inventory, "Inventory cannot be null");
        Integer quantity = inventory.getQuantity();
        Integer reserved = inventory.getQuantityReserved();
        
        // Handle case where quantityReserved is null
        if (reserved == null) {
            reserved = 0;
        }
        
        return quantity - reserved;
    }
}
