package dev.dini.scms.inventory.service;

import dev.dini.scms.inventory.entity.Inventory;
import dev.dini.scms.util.InventoryUtil;
import dev.dini.scms.util.exception.InsufficientInventoryException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final InventoryService inventoryService;
    private final InventoryUtil inventoryUtil;
    private final InventoryCalculationService calculationService;

    @Override
    @Transactional
    public void reserveInventory(Long productId, int quantity) {
        log.info("Reserving inventory for product ID {} with quantity {}", productId, quantity);
        updateReservation(productId, quantity, true);
    }

    @Override
    @Transactional
    public void confirmReservation(Long productId, int quantity) {
        log.info("Confirming reservation for product ID {} with quantity {}", productId, quantity);
        updateReservation(productId, quantity, false);
    }

    @Override
    @Transactional
    public void releaseReservation(Long productId, int quantity) {
        log.info("Releasing reservation for product ID {} with quantity {}", productId, quantity);
        updateReservation(productId, quantity, false);
    }

    private void updateReservation(Long productId, int quantity, boolean isReservation) {

        /*
         * Check if the product ID is valid and if the inventory exists.
         * If not, throw an InventoryNotFoundException.
         */
        Inventory inventory = inventoryUtil.findInventoryByProductId(productId);

        // Initialize quantityReserved to 0 if it's null
        if (inventory.getQuantityReserved() == null) {
            inventory.setQuantityReserved(0);
        }

        Integer availableQuantity = calculationService.getAvailableQuantity(inventory);

        if (isReservation) {
            if (availableQuantity == null || availableQuantity < quantity) {
                throw new InsufficientInventoryException("Insufficient available inventory for product ID: " + productId);
            }
            inventory.setQuantityReserved(inventory.getQuantityReserved() + quantity);
            log.info("{} units reserved for product ID {}. Available: {}, Reserved: {}",
                    quantity, productId, availableQuantity, inventory.getQuantityReserved());
        } else {
            if (inventory.getQuantityReserved() < quantity) {
                throw new IllegalStateException("Cannot confirm/release more items than reserved for product ID: " + productId);
            }
            inventory.setQuantity(inventory.getQuantity() - quantity);
            inventory.setQuantityReserved(inventory.getQuantityReserved() - quantity);
            log.info("{} units {} for product ID {}. New total: {}, Reserved: {}",
                    quantity, "released", productId,
                    inventory.getQuantity(), inventory.getQuantityReserved());
        }
        inventoryService.saveInventory(inventory);
    }
}
