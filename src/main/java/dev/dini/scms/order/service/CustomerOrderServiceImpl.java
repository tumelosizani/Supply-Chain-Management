package dev.dini.scms.order.service;

import dev.dini.scms.inventory.service.InventoryService;
import dev.dini.scms.inventory.service.ReservationService;
import dev.dini.scms.order.dto.*;
import dev.dini.scms.order.entity.*;
import dev.dini.scms.util.exception.CustomerOrderNotFoundException;
import dev.dini.scms.util.exception.InsufficientInventoryException;
import dev.dini.scms.order.mapper.CustomerOrderMapper;
import dev.dini.scms.order.repository.CustomerOrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerOrderServiceImpl implements CustomerOrderService {

    private final CustomerOrderRepository customerOrderRepository;
    private final CustomerOrderMapper customerOrderMapper;
    private final CustomerOrderItemService customerOrderItemService;
    private final InventoryService inventoryService;
    private final ReservationService reservationService;

    @Override
    @Transactional
    public CustomerOrderResponseDTO createCustomerOrder(CustomerOrderRequestDTO createDTO) {
        log.info("Creating customer order {}", createDTO);

        // Check inventory availability before proceeding
        if (!checkInventoryAvailability(createDTO.items())) {
            throw new InsufficientInventoryException("Insufficient inventory for one or more items in the order");
        }

        CustomerOrder order = customerOrderMapper.toEntity(createDTO);
        order.setStatus(CustomerOrderStatus.PENDING);
        order = customerOrderRepository.save(order); // Save the order first to get the ID

        List<CustomerOrderItem> orderItems = new ArrayList<>();
        for (CustomerOrderItemRequestDTO itemDTO : createDTO.items()) {
            CustomerOrderItemRequestDTO itemRequestDTO = new CustomerOrderItemRequestDTO(
                    itemDTO.productId(),
                    itemDTO.quantity(),
                    order.getId() // Now we have the order ID to pass
            );
            CustomerOrderItemResponseDTO itemResponseDTO = customerOrderItemService.createCustomerOrderItem(itemRequestDTO);
            CustomerOrderItem orderItem = customerOrderItemService.getEntityById(itemResponseDTO.id());
            orderItems.add(orderItem);
        }
        order.setItems(orderItems);

        // Reserve the inventory for the order
        reserveInventory(createDTO.items());

        // Update order status to PROCESSING after reservation
        order.setStatus(CustomerOrderStatus.PROCESSING);
        
        CustomerOrder savedOrder = customerOrderRepository.save(order);
        log.info("Customer order created with status {} and ID {}", savedOrder.getStatus(), savedOrder.getId());
        return customerOrderMapper.toResponseDTO(savedOrder);
    }

    @Override
    @Transactional
    public CustomerOrderResponseDTO updateCustomerOrder(Long id, CustomerOrderRequestDTO updateDTO) {
        log.info("Updating customer order {}", updateDTO);
        CustomerOrder customerOrder = findCustomerOrderById(id);
        customerOrderMapper.partialUpdate(updateDTO, customerOrder);
        CustomerOrder updatedCustomerOrder = customerOrderRepository.save(customerOrder);
        log.info("Customer order updated {}", updatedCustomerOrder);
        return customerOrderMapper.toResponseDTO(updatedCustomerOrder);
    }

    @Override
    public void deleteCustomerOrder(Long id) {
        log.info("Deleting customer order {}", id);
        customerOrderRepository.deleteById(id);
    }

    @Override
    public CustomerOrderResponseDTO getCustomerOrderById(Long id) {
        log.info("Getting customer order {}", id);
        CustomerOrder customerOrder = findCustomerOrderById(id);
        return customerOrderMapper.toResponseDTO(customerOrder);
    }

    @Override
    public CustomerOrder getEntityById(Long id) {
        return customerOrderRepository.findById(id)
                .orElseThrow(() -> new CustomerOrderNotFoundException(id));
    }

    @Override
    @Transactional
    public CustomerOrderResponseDTO addItemToOrder(Long orderId, CustomerOrderItemRequestDTO itemRequestDTO) {
        log.info("Adding item to order {}", orderId);
        CustomerOrder order = findCustomerOrderById(orderId);

        //  Set the customerOrderId in the DTO.
        CustomerOrderItemRequestDTO itemRequest = new CustomerOrderItemRequestDTO(
                itemRequestDTO.productId(),
                itemRequestDTO.quantity(),
                orderId
        );
        CustomerOrderItemResponseDTO itemResponseDTO = customerOrderItemService.createCustomerOrderItem(itemRequest);
        CustomerOrderItem orderItem = customerOrderItemService.getEntityById(itemResponseDTO.id());
        order.getItems().add(orderItem);

        CustomerOrder updatedOrder = customerOrderRepository.save(order);
        log.info("Item added to order");
        return customerOrderMapper.toResponseDTO(updatedOrder);
    }

    @Override
    @Transactional
    public CustomerOrderResponseDTO updateOrderItem(Long orderId, Long itemId, CustomerOrderItemRequestDTO itemRequestDTO) {
        log.info("Updating item {} in order {}", itemId, orderId);
        CustomerOrder order = findCustomerOrderById(orderId);

        CustomerOrderItem itemToUpdate = order.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException(
                        "Order item with id " + itemId + " not found in order " + orderId));

        // Update the item's properties directly
        itemToUpdate.setQuantity(itemRequestDTO.quantity());  // Example of updating quantity.  Add other fields as needed.

        CustomerOrder updatedOrder = customerOrderRepository.save(order);
        log.info("Item updated in order");
        return customerOrderMapper.toResponseDTO(updatedOrder);
    }

    @Override
    @Transactional
    public CustomerOrderResponseDTO removeItemFromOrder(Long orderId, Long itemId) {
        log.info("Removing item {} from order {}", itemId, orderId);
        CustomerOrder order = findCustomerOrderById(orderId);

        boolean removed = order.getItems().removeIf(item -> item.getId().equals(itemId));
        if (!removed) {
            throw new NoSuchElementException(
                    "Order item with id " + itemId + " not found in order " + orderId);
        }

        CustomerOrder updatedOrder = customerOrderRepository.save(order);
        log.info("Item removed from order");
        return customerOrderMapper.toResponseDTO(updatedOrder);
    }

    @Override
    public boolean checkInventoryAvailability(List<CustomerOrderItemRequestDTO> items) {
        log.info("Checking inventory availability for {} items", items.size());

        for (CustomerOrderItemRequestDTO item : items) {
            if (!inventoryService.isInventoryAvailable(item.productId(), item.quantity())) {
                log.warn("Insufficient inventory for product ID: {}, requested quantity: {}",
                        item.productId(), item.quantity());
                return false;
            }
        }

        log.info("Inventory check passed for all items");
        return true;
    }

    @Override
    @Transactional
    public void reserveInventory(List<CustomerOrderItemRequestDTO> items) {
        log.info("Reserving inventory for {} items", items.size());

        for (CustomerOrderItemRequestDTO item : items) {
            reservationService.reserveInventory(item.productId(), item.quantity());
            log.info("Reserved {} units of product ID: {}", item.quantity(), item.productId());
        }

        log.info("Inventory successfully reserved for all items");
    }
    
    @Override
    @Transactional
    public CustomerOrderResponseDTO confirmOrder(Long orderId) {
        log.info("Confirming order with ID {}", orderId);
        CustomerOrder order = findCustomerOrderById(orderId);
        
        if (order.getStatus() != CustomerOrderStatus.PROCESSING) {
            throw new IllegalStateException("Order cannot be confirmed because it is not in PROCESSING state. Current state: " + order.getStatus());
        }
        
        // Confirm the reservation (reduce actual inventory)
        for (CustomerOrderItem item : order.getItems()) {
            reservationService.confirmReservation(item.getProduct().getId(), item.getQuantity());
            log.info("Confirmed reservation of {} units of product ID: {}", 
                    item.getQuantity(), item.getProduct().getId());
        }
        
        order.setStatus(CustomerOrderStatus.CONFIRMED);
        CustomerOrder confirmedOrder = customerOrderRepository.save(order);
        log.info("Order confirmed with ID {}", confirmedOrder.getId());
        
        return customerOrderMapper.toResponseDTO(confirmedOrder);
    }
    
    @Override
    @Transactional
    public CustomerOrderResponseDTO cancelOrder(Long orderId) {
        log.info("Cancelling order with ID {}", orderId);
        CustomerOrder order = findCustomerOrderById(orderId);
        
        // Only allow cancellation of orders in PENDING or PROCESSING state
        if (order.getStatus() != CustomerOrderStatus.PENDING && 
            order.getStatus() != CustomerOrderStatus.PROCESSING) {
            throw new IllegalStateException("Order cannot be cancelled because it is in " + order.getStatus() + " state");
        }
        
        // Release any reserved inventory
        if (order.getStatus() == CustomerOrderStatus.PROCESSING) {
            for (CustomerOrderItem item : order.getItems()) {
                reservationService.releaseReservation(item.getProduct().getId(), item.getQuantity());
                log.info("Released reservation of {} units of product ID: {}", 
                        item.getQuantity(), item.getProduct().getId());
            }
        }
        
        order.setStatus(CustomerOrderStatus.CANCELLED);
        CustomerOrder cancelledOrder = customerOrderRepository.save(order);
        log.info("Order cancelled with ID {}", cancelledOrder.getId());
        
        return customerOrderMapper.toResponseDTO(cancelledOrder);
    }

    private CustomerOrder findCustomerOrderById(Long id) {
        log.info("Finding customer order by id {}", id);
        return customerOrderRepository.findById(id)
                .orElseThrow(() -> new CustomerOrderNotFoundException(id));
    }
}
