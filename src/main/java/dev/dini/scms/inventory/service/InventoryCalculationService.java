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

        // Treat missing physical quantity or reservation as zero rather than NPE-ing.
        if (quantity == null) {
            quantity = 0;
        }
        if (reserved == null) {
            reserved = 0;
        }

        return quantity - reserved;
    }
}
