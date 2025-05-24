package dev.dini.scms.inventory.service.reserve;

import dev.dini.scms.order.dto.CustomerOrderItemRequestDTO;

import java.util.List;

public interface ReservationService {


    /**
     * Reserves the quantity of inventory for a given product without reducing the total quantity.
     * This increases the quantityReserved field.
     *
     * @param productId the ID of the product
     * @param quantity the quantity to reserve
     */
    void reserveInventory(Long productId, int quantity);

    /**
     * Confirms the inventory reservation by reducing the actual quantity and the reserved quantity.
     * This should be called when the order is confirmed/shipped.
     *
     * @param productId the ID of the product
     * @param quantity the quantity to confirm
     */
    void confirmReservation(Long productId, int quantity);

    /**
     * Releases a previously reserved inventory without reducing the actual quantity.
     * This decreases the quantityReserved field.
     *
     * @param productId the ID of the product
     * @param quantity the quantity to release
     */
    void releaseReservation(Long productId, int quantity);

    void reserveInventoryBatch(List<CustomerOrderItemRequestDTO> items);

}
