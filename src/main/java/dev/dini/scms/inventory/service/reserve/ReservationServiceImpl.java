package dev.dini.scms.inventory.service.reserve;

import dev.dini.scms.inventory.dto.InventoryReservationItemDTO;
import dev.dini.scms.inventory.entity.Inventory;
import dev.dini.scms.inventory.service.InventoryCalculationService;
import dev.dini.scms.inventory.service.InventoryService;
import dev.dini.scms.inventory.service.InventoryUtil;
import dev.dini.scms.util.exception.InsufficientInventoryException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final InventoryService inventoryService;
    private final InventoryUtil inventoryUtil;
    private final InventoryCalculationService calculationService;

    private enum ReservationOp { RESERVE, CONFIRM, RELEASE }

    @Override
    @Transactional
    public void reserveInventory(Long productId, int quantity) {
        log.info("Reserving inventory for product ID {} with quantity {}", productId, quantity);
        updateReservation(productId, quantity, ReservationOp.RESERVE);
    }

    @Override
    @Transactional
    public void confirmReservation(Long productId, int quantity) {
        log.info("Confirming reservation for product ID {} with quantity {}", productId, quantity);
        updateReservation(productId, quantity, ReservationOp.CONFIRM);
    }

    @Override
    @Transactional
    public void releaseReservation(Long productId, int quantity) {
        log.info("Releasing reservation for product ID {} with quantity {}", productId, quantity);
        updateReservation(productId, quantity, ReservationOp.RELEASE);
    }

    @Override
    @Transactional
    public void reserveInventoryBatch(List<InventoryReservationItemDTO> items) {
        log.info("Reserving inventory for {} items in batch", items.size());

        for (InventoryReservationItemDTO item : items) {
            if (item.productId() == null || item.quantity() <= 0) {
                throw new IllegalArgumentException(
                        "Invalid item: Product ID and quantity must be set and positive. Found: Product ID = "
                                + item.productId() + ", Quantity = " + item.quantity()
                );
            }
        }

        // Group items by productId in case duplicates exist
        Map<Long, Integer> productIdToQuantityMap = new HashMap<>();
        for (var item : items) {
            productIdToQuantityMap.merge(item.productId(), item.quantity(), Integer::sum);
        }

        for (Map.Entry<Long, Integer> entry : productIdToQuantityMap.entrySet()) {
            Long productId = entry.getKey();
            int totalQuantity = entry.getValue();

            // Use existing reservation logic
            reserveInventory(productId, totalQuantity);
            log.info("Reserved {} units of product ID: {}", totalQuantity, productId);
        }

        log.info("Batch inventory reservation completed");
    }


    private void updateReservation(Long productId, int quantity, ReservationOp op) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Reservation quantity must be positive for product ID: " + productId);
        }

        /*
         * Check if the product ID is valid and if the inventory exists.
         * If not, throw an InventoryNotFoundException.
         */
        var inventory = inventoryUtil.findInventoryByProductId(productId);

        // Initialize quantityReserved to 0 if it's null
        if (inventory.getQuantityReserved() == null) {
            inventory.setQuantityReserved(0);
        }

        switch (op) {
            case RESERVE -> {
                Integer availableQuantity = calculationService.getAvailableQuantity(inventory);
                if (availableQuantity == null || availableQuantity < quantity) {
                    throw new InsufficientInventoryException("Insufficient available inventory for product ID: " + productId);
                }
                inventory.setQuantityReserved(inventory.getQuantityReserved() + quantity);
                log.info("{} units reserved for product ID {}. Available before: {}, Reserved now: {}",
                        quantity, productId, availableQuantity, inventory.getQuantityReserved());
            }
            case CONFIRM -> {
                if (inventory.getQuantityReserved() < quantity) {
                    throw new IllegalStateException("Cannot confirm more items than reserved for product ID: " + productId);
                }
                // Stock physically leaves the warehouse: reduce both on-hand quantity and the held reservation.
                inventory.setQuantity(inventory.getQuantity() - quantity);
                inventory.setQuantityReserved(inventory.getQuantityReserved() - quantity);
                log.info("{} units confirmed (deducted) for product ID {}. New total: {}, Reserved: {}",
                        quantity, productId, inventory.getQuantity(), inventory.getQuantityReserved());
            }
            case RELEASE -> {
                if (inventory.getQuantityReserved() < quantity) {
                    throw new IllegalStateException("Cannot release more items than reserved for product ID: " + productId);
                }
                // Reservation is freed back to available stock; physical quantity is unchanged.
                inventory.setQuantityReserved(inventory.getQuantityReserved() - quantity);
                log.info("{} units released for product ID {}. Total: {}, Reserved: {}",
                        quantity, productId, inventory.getQuantity(), inventory.getQuantityReserved());
            }
        }
        inventoryService.saveInventory(inventory);
    }
}
