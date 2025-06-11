package dev.dini.scms.order.service;

import dev.dini.scms.inventory.service.*;
import dev.dini.scms.inventory.service.availbitity.InventoryAvailabilityService;
import dev.dini.scms.inventory.service.reserve.ReservationService;
import dev.dini.scms.order.dto.*;
import dev.dini.scms.order.entity.*;
import dev.dini.scms.order.mapper.*;
import dev.dini.scms.order.repository.CustomerOrderRepository;
import dev.dini.scms.order.service.calulation.OrderCalculationService;
import dev.dini.scms.product.service.ProductService;
import dev.dini.scms.util.exception.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerOrderServiceImpl implements CustomerOrderService {

    private final CustomerOrderRepository repository;
    private final CustomerOrderMapper customerOrderMapper;
    private final CustomerOrderItemMapper customerOrderItemMapper;
    private final InventoryService inventoryService;
    private final ReservationService reservationService;
    private final ProductService productService;
    private final InventoryAvailabilityService inventoryAvailabilityService;
    private final CustomerOrderValidator customerOrderValidator;
    private final OrderCalculationService orderCalculationService;


    @Override
    @Transactional
    public CustomerOrderResponseDTO createCustomerOrder(CustomerOrderRequestDTO createDTO) {
        log.info("Creating customer order: {}", createDTO);

        customerOrderValidator.validateItems(createDTO.items());
        inventoryAvailabilityService.ensureSufficientInventory(createDTO.items());

        var order = customerOrderMapper.toEntity(createDTO);
        order.setOrderDate(new Date());
        order.setStatus(CustomerOrderStatus.PENDING);

        List<CustomerOrderItem> items = createOrderItemsFromDTOs(createDTO.items(), order);
        order.setItems(new ArrayList<>(items));

        CustomerOrder savedOrder = repository.save(order);

        orderCalculationService.calculateTotalCost(savedOrder);
        savedOrder = repository.save(savedOrder);

        reservationService.reserveInventoryBatch(createDTO.items());

        log.info("Customer order created: {}", savedOrder);
        return customerOrderMapper.toResponseDTO(savedOrder);
    }

    private List<CustomerOrderItem> createOrderItemsFromDTOs(List<CustomerOrderItemRequestDTO> itemDTOs, CustomerOrder order) {
        return itemDTOs.stream()
                .map(itemDTO -> {
                    if (itemDTO.productId() == null) {
                        throw new IllegalArgumentException("Product ID cannot be null for an order item.");
                    }

                    var product = productService.getEntityById(itemDTO.productId());
                    if (product.getUnitPrice() == null) {
                        log.error("Product with ID {} has a null unitPrice. Order item cannot be priced correctly.", product.getId());
                        throw new IllegalStateException("Product " + product.getId() + " has no price defined, cannot add to order.");
                    }

                    var orderItem = customerOrderItemMapper.toEntity(itemDTO);
                    orderItem.setProduct(product);
                    orderItem.setPrice(product.getUnitPrice());
                    orderItem.setCustomerOrder(order);
                    return orderItem;
                })
                .toList();
    }

    @Override
    @Transactional
    public CustomerOrderResponseDTO updateCustomerOrder(Long orderId, CustomerOrderRequestDTO updateDTO) {
        log.info("Attempting to update customer order with ID {}, using DTO: {}", orderId, updateDTO);
        var customerOrder = findCustomerOrderById(orderId);

        // Restrict general updates to PENDING orders for data integrity
        if (customerOrder.getStatus() != CustomerOrderStatus.PENDING) {
            log.warn("Attempting to update order {} which is in {} state. General updates (e.g., shipping address) are only applicable to PENDING orders.", orderId, customerOrder.getStatus());
            throw new IllegalStateException("Order cannot be generally updated in its current state: " + customerOrder.getStatus());
        }

        customerOrderMapper.partialUpdate(updateDTO, customerOrder); // Update simple fields from DTO
        CustomerOrder updatedCustomerOrder = repository.save(customerOrder);

        log.info("Customer order updated: {}", updatedCustomerOrder);
        return customerOrderMapper.toResponseDTO(updatedCustomerOrder);
    }

    @Override
    @Transactional
    public void deleteCustomerOrder(Long orderId) {
        log.info("Attempting to delete customer order with ID: {}", orderId);
        CustomerOrder order = findCustomerOrderById(orderId);


        if (order.getStatus() == CustomerOrderStatus.CONFIRMED ||
                order.getStatus() == CustomerOrderStatus.SHIPPED ||
                order.getStatus() == CustomerOrderStatus.DELIVERED ||
                order.getStatus() == CustomerOrderStatus.PROCESSING) { // Also prevent deletion if processing
            log.error("Cannot delete order {} as it is in {} state. Deletion not allowed for fulfilled or actively processing orders.", orderId, order.getStatus());
            throw new IllegalStateException("Order cannot be deleted in its current state: " + order.getStatus());
        }


        for (CustomerOrderItem item : order.getItems()) {

            if (item.getProduct() != null && item.getProduct().getId() != null && item.getQuantity() > 0) {
                reservationService.releaseReservation(item.getProduct().getId(), item.getQuantity());
                log.info("Released reservation for product ID: {}, quantity: {} during order deletion of order ID {}",
                        item.getProduct().getId(), item.getQuantity(), orderId);
            } else {
                log.warn("Could not release reservation for an item in order {} as product or product ID is null or quantity non-positive. Item ID: {}. This might indicate a data inconsistency.",
                        orderId, item.getId());
            }
        }
        repository.deleteById(orderId);
        log.info("Customer order with ID {} deleted successfully", orderId);
    }

    @Override
    public CustomerOrderResponseDTO getCustomerOrderById(Long orderId) {
        log.info("Fetching customer order by ID: {}", orderId);
        var customerOrder = findCustomerOrderById(orderId);
        log.info("Returning customer order: {}", customerOrder);
        return customerOrderMapper.toResponseDTO(customerOrder);
    }

    @Override
    public CustomerOrder getEntityById(Long orderId) {
        return repository.findById(orderId)
                .orElseThrow(() -> new CustomerOrderNotFoundException(orderId));
    }

    @Override
    @Transactional
    public CustomerOrderResponseDTO addItemToOrder(Long orderId, CustomerOrderItemRequestDTO itemRequestDTO) {
        log.info("Attempting to add item to order ID {}: {}", orderId, itemRequestDTO);
        var order = findCustomerOrderById(orderId);

        // Allow item modification only for PENDING orders
        if (order.getStatus() != CustomerOrderStatus.PENDING) {
            throw new IllegalStateException("Order " + orderId + " cannot be modified as it is in " + order.getStatus() + " state.");
        }
        if (itemRequestDTO.productId() == null) {
            throw new IllegalArgumentException("Product ID cannot be null for the item to be added.");
        }
        if (itemRequestDTO.quantity() <= 0) {
            throw new IllegalArgumentException("Item quantity must be positive.");
        }

        if (inventoryService.isInventoryAvailable(itemRequestDTO.productId(), itemRequestDTO.quantity())) {
            throw new InsufficientInventoryException("Insufficient inventory for product ID: " + itemRequestDTO.productId() + ", requested quantity: " + itemRequestDTO.quantity());
        }


        var product = productService.getEntityById(itemRequestDTO.productId());
        if (product.getUnitPrice() == null) {
            log.error("Product with ID {} has a null unitPrice. Cannot add item to order.", product.getId());
            throw new IllegalStateException("Product " + product.getId() + " has no price, cannot add to order.");
        }

        var orderItem = customerOrderItemMapper.toEntity(itemRequestDTO);
        orderItem.setProduct(product);
        orderItem.setPrice(product.getUnitPrice());
        orderItem.setCustomerOrder(order);

        order.getItems().add(orderItem);


        var updatedOrder = repository.save(order);
        recalculateAndSave(updatedOrder);

        reservationService.reserveInventory(itemRequestDTO.productId(), itemRequestDTO.quantity());
        log.info("Item added to order ID {}. Reserved quantity {} of product ID {}", orderId, itemRequestDTO.quantity(), itemRequestDTO.productId());
        return customerOrderMapper.toResponseDTO(updatedOrder);
    }

    @Override
    @Transactional
    public CustomerOrderResponseDTO updateOrderItem(Long orderId, Long itemId, CustomerOrderItemRequestDTO itemRequestDTO) {
        log.info("Attempting to update item ID {} in order ID {} with DTO: {}", itemId, orderId, itemRequestDTO);
        CustomerOrder order = findCustomerOrderById(orderId);

        // Allow item modification only for PENDING orders
        if (order.getStatus() != CustomerOrderStatus.PENDING) {
            throw new IllegalStateException("Order " + orderId + " item cannot be modified as the order is in " + order.getStatus() + " state.");
        }
        if (itemRequestDTO.quantity() <= 0) {
            throw new IllegalArgumentException("Item quantity must be positive.");
        }

        CustomerOrderItem itemToUpdate = findOrderItemInOrder(order, itemId); // Find the specific item

        int oldQuantity = itemToUpdate.getQuantity();
        int newQuantity = itemRequestDTO.quantity();
        int quantityDifference = newQuantity - oldQuantity;

        // Check for sufficient inventory if quantity is increasing
        // Using your `inventoryService.isInventoryAvailable` method with corrected logic
        if (quantityDifference > 0) {
            if (inventoryService.isInventoryAvailable(itemToUpdate.getProduct().getId(), quantityDifference)) {
                throw new InsufficientInventoryException("Insufficient inventory to increase order item quantity for product ID: " + itemToUpdate.getProduct().getId() + ", quantity needed: " + quantityDifference);
            }
        }

        itemToUpdate.setQuantity(newQuantity); // Update quantity
        // Save to persist the updated item quantity
        CustomerOrder savedOrder = repository.save(order);

        // Recalculate and persist the total cost after item quantity update
        recalculateAndSave(savedOrder);
        savedOrder = repository.save(savedOrder); // Save again to persist updated totalCost

        // Adjust inventory reservation based on quantity difference
        if (quantityDifference != 0) {
            if (quantityDifference > 0) {
                reservationService.reserveInventory(itemToUpdate.getProduct().getId(), quantityDifference);
                log.info("Reserved {} more units of product ID {} for order item update", quantityDifference, itemToUpdate.getProduct().getId());
            } else {
                reservationService.releaseReservation(itemToUpdate.getProduct().getId(), Math.abs(quantityDifference));
                log.info("Released {} units of product ID {} for order item update", Math.abs(quantityDifference), itemToUpdate.getProduct().getId());
            }
        }
        log.info("Item ID {} in order ID {} updated to quantity {}", itemId, orderId, newQuantity);
        return customerOrderMapper.toResponseDTO(savedOrder);
    }

    @Override
    @Transactional
    public CustomerOrderResponseDTO removeItemFromOrder(Long orderId, Long itemId) {
        log.info("Attempting to remove item ID {} from order ID {}", itemId, orderId);
        var order = findCustomerOrderById(orderId);

        if (order.getStatus() != CustomerOrderStatus.PENDING) {
            throw new IllegalStateException("Order " + orderId + " item cannot be removed as the order is in " + order.getStatus() + " state.");
        }

        CustomerOrderItem itemToRemove = findOrderItemInOrder(order, itemId);

        if (itemToRemove.getProduct() != null && itemToRemove.getProduct().getId() != null && itemToRemove.getQuantity() > 0) {
            reservationService.releaseReservation(itemToRemove.getProduct().getId(), itemToRemove.getQuantity());
            log.info("Released {} units of product ID {} for removed order item", itemToRemove.getQuantity(), itemToRemove.getProduct().getId());
        } else {
            log.warn("Could not release reservation for removed item ID {} in order {}: Product or Product ID is null or quantity non-positive. This might indicate a data inconsistency.", itemId, orderId);
        }

        boolean removed = order.getItems().removeIf(item -> item.getId() != null && item.getId().equals(itemId));
        if (!removed) {
            log.warn("Item ID {} was not found in order {} items list during removal after being fetched. This is unexpected and should be investigated.", itemId, orderId);
        }

        var updatedOrder = repository.save(order);

        recalculateAndSave(updatedOrder);

        log.info("Item ID {} removed from order ID {}", itemId, orderId);
        return customerOrderMapper.toResponseDTO(updatedOrder);
    }

    /**
     * Helper method to find a specific CustomerOrderItem within a CustomerOrder's items collection.
     *
     * @param order The CustomerOrder to search within.
     * @param itemId The ID of the CustomerOrderItem to find.
     * @return The found CustomerOrderItem.
     * @throws IllegalArgumentException if itemId is null.
     * @throws NoSuchElementException if the item is not found in the order.
     */
    private CustomerOrderItem findOrderItemInOrder(CustomerOrder order, Long itemId) {
        if (itemId == null) {
            throw new IllegalArgumentException("Item ID cannot be null when searching in order.");
        }
        return order.getItems().stream()
                .filter(item -> item.getId() != null && item.getId().equals(itemId)) // Filter by ID, ensuring ID is not null
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException(
                        "Order item with id " + itemId + " not found in order " + order.getId()));
    }


    @Override
    @Transactional
    public void processOrder(Long orderId) {
        log.info("Attempting to move order ID {} to PROCESSING state", orderId);
        var order = findCustomerOrderById(orderId);

        if (order.getStatus() != CustomerOrderStatus.PENDING) {
            throw new IllegalStateException("Order " + orderId + " cannot be processed because it is not in PENDING state. Current state: " + order.getStatus());
        }

        order.setStatus(CustomerOrderStatus.PROCESSING);
        var processedOrder = repository.save(order);
        log.info("Order ID {} is now in PROCESSING state", processedOrder.getId());
    }

    @Override
    @Transactional
    public CustomerOrderResponseDTO confirmOrder(Long orderId) {
        log.info("Attempting to confirm order with ID {}", orderId);
        var order = findCustomerOrderById(orderId);

        if (order.getStatus() != CustomerOrderStatus.PROCESSING) {
            throw new IllegalStateException("Order " + orderId + " cannot be confirmed because it is not in PROCESSING state. Current state: " + order.getStatus());
        }

        // Confirm (deduct) inventory reservations for each item
        for (CustomerOrderItem item : order.getItems()) {
            // Added null checks for robustness
            if (item.getProduct() != null && item.getProduct().getId() != null && item.getQuantity() > 0) {
                reservationService.confirmReservation(item.getProduct().getId(), item.getQuantity());
                log.info("Confirmed reservation (inventory deducted) of {} units of product ID: {} for order ID {}",
                        item.getQuantity(), item.getProduct().getId(), orderId);
            } else {
                log.error("Cannot confirm reservation for an item in order {} as product/product ID is null or quantity is non-positive. Item ID: {}. Skipping confirmation for this item.",
                        orderId, item.getId());
            }
        }

        order.setStatus(CustomerOrderStatus.CONFIRMED);
        var confirmedOrder = repository.save(order);
        log.info("Order ID {} confirmed successfully", confirmedOrder.getId());

        return customerOrderMapper.toResponseDTO(confirmedOrder);
    }

    @Override
    @Transactional
    public CustomerOrderResponseDTO cancelOrder(Long orderId) {
        log.info("Attempting to cancel order with ID {}", orderId);
        CustomerOrder order = findCustomerOrderById(orderId);

        // Define which states allow cancellation
        if (order.getStatus() != CustomerOrderStatus.PENDING &&
                order.getStatus() != CustomerOrderStatus.PROCESSING) {
            log.warn("Order {} cannot be cancelled because it is in {} state. Cancellation is only allowed for PENDING or PROCESSING orders.", orderId, order.getStatus());
            throw new IllegalStateException("Order cannot be cancelled in its current state: " + order.getStatus());
        }

        // Release reservations if order was in a state where inventory was held
        for (CustomerOrderItem item : order.getItems()) {
            // Added null checks for robustness
            if (item.getProduct() != null && item.getProduct().getId() != null && item.getQuantity() > 0) {
                reservationService.releaseReservation(item.getProduct().getId(), item.getQuantity());
                log.info("Released reservation of {} units of product ID: {} for cancelled order ID {}",
                        item.getQuantity(), item.getProduct().getId(), orderId);
            } else {
                log.error("Cannot release reservation for an item in cancelled order {} as product/product ID is null or quantity non-positive. Item ID: {}. Skipping release for this item.",
                        orderId, item.getId());
            }
        }

        order.setStatus(CustomerOrderStatus.CANCELLED);
        CustomerOrder cancelledOrder = repository.save(order);
        log.info("Order ID {} cancelled successfully", cancelledOrder.getId());
        return customerOrderMapper.toResponseDTO(cancelledOrder);
    }

    @Override
    public List<CustomerOrderResponseDTO> getAllCustomerOrders() {
        log.info("Fetching all customer orders");
        List<CustomerOrder> orders = repository.findAll();

        if (orders.isEmpty()) {
            log.info("No customer orders found.");
        }
        return orders.stream()
                .map(customerOrderMapper::toResponseDTO)
                .toList();
    }

    private CustomerOrder findCustomerOrderById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Order ID cannot be null.");
        }
        log.debug("Attempting to find customer order by ID {}", id);
        return repository.findById(id)
                .orElseThrow(() -> new CustomerOrderNotFoundException(id));
    }

    private void recalculateAndSave(CustomerOrder order) {
        orderCalculationService.calculateTotalCost(order);
        repository.save(order);
    }

}