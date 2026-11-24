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
     * Transitions a PENDING order to PROCESSING.
     *
     * @param orderId the ID of the order to process
     * @return the updated order response
     */
    CustomerOrderResponseDTO processOrder(Long orderId);

    /**
     * Marks a CONFIRMED order as SHIPPED.
     *
     * @param orderId the ID of the order to mark as shipped
     * @return the updated order response
     */
    CustomerOrderResponseDTO markShipped(Long orderId);

    /**
     * Marks a SHIPPED order as DELIVERED.
     *
     * @param orderId the ID of the order to mark as delivered
     * @return the updated order response
     */
    CustomerOrderResponseDTO markDelivered(Long orderId);

    /**
     * Confirms a PROCESSING order by deducting reserved inventory and changing its status to CONFIRMED.
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

    /**
     * Retrieves a list of all customer orders.
     *
     * @return a list of customer order response DTOs
     */
    List<CustomerOrderResponseDTO> getAllCustomerOrders();
}
