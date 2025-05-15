package dev.dini.scms.order.service;


import dev.dini.scms.order.dto.*;
import dev.dini.scms.order.entity.CustomerOrder;

import java.util.List;

public interface CustomerOrderService {
    CustomerOrderResponseDTO createCustomerOrder(CustomerOrderRequestDTO createDTO);

    CustomerOrderResponseDTO updateCustomerOrder(Long id, CustomerOrderRequestDTO updateDTO);

    void deleteCustomerOrder(Long id);

    CustomerOrderResponseDTO getCustomerOrderById(Long id);

    CustomerOrder getEntityById(Long id);


    CustomerOrderResponseDTO addItemToOrder(Long orderId, CustomerOrderItemRequestDTO itemRequestDTO);

    CustomerOrderResponseDTO updateOrderItem(Long orderId, Long itemId, CustomerOrderItemRequestDTO itemRequestDTO);

    CustomerOrderResponseDTO removeItemFromOrder(Long orderId, Long itemId);

    /**
     * Checks if there is sufficient inventory for all items in the order.
     *
     * @param items the list of order items to check
     * @return true if all items have sufficient inventory, false otherwise
     */
    boolean checkInventoryAvailability(List<CustomerOrderItemRequestDTO> items);
    
    /**
     * Reserves inventory for all items in the order by increasing the reserved quantities.
     *
     * @param items the list of order items to reserve inventory for
     */
    void reserveInventory(List<CustomerOrderItemRequestDTO> items);
    
    /**
     * Confirms an order by changing its status to PROCESSING after inventory has been reserved.
     *
     * @param orderId the ID of the order to confirm
     * @return the updated order response
     */
    CustomerOrderResponseDTO confirmOrder(Long orderId);
    
    /**
     * Cancels an order and releases any reserved inventory.
     *
     * @param orderId the ID of the order to cancel
     * @return the updated order response
     */
    CustomerOrderResponseDTO cancelOrder(Long orderId);
}
