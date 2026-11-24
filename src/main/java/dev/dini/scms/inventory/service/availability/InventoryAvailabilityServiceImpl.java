package dev.dini.scms.inventory.service.availability;

import dev.dini.scms.inventory.dto.InventoryReservationItemDTO;
import dev.dini.scms.inventory.entity.Inventory;
import dev.dini.scms.inventory.service.InventoryCalculationService;
import dev.dini.scms.inventory.service.InventoryUtil;
import dev.dini.scms.util.exception.InsufficientInventoryException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryAvailabilityServiceImpl implements InventoryAvailabilityService {

    private final InventoryUtil inventoryUtil;
    private final InventoryCalculationService calculationService;

    @Override
    public void ensureSufficientInventory(List<InventoryReservationItemDTO> items) {
        for (var item : items) {
            Inventory inventory = inventoryUtil.findInventoryByProductId(item.productId());
            int available = calculationService.getAvailableQuantity(inventory);
            if (available < item.quantity()) {
                throw new InsufficientInventoryException("Insufficient inventory for product ID: " + item.productId());
            }
        }
    }
}
