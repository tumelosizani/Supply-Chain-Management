package dev.dini.scms.util;

import dev.dini.scms.inventory.entity.Inventory;
import dev.dini.scms.inventory.repository.InventoryRepository;
import dev.dini.scms.util.exception.InventoryNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InventoryUtil {

    private final InventoryRepository inventoryRepository;

    public Inventory findInventoryByProductId(Long productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new InventoryNotFoundException("Inventory not found for product ID: " + productId));
    }
}
