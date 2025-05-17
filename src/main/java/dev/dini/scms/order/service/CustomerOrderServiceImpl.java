package dev.dini.scms.order.service;

import dev.dini.scms.inventory.service.*;
import dev.dini.scms.order.dto.*;
import dev.dini.scms.order.entity.*;
import dev.dini.scms.order.mapper.*;
import dev.dini.scms.order.repository.CustomerOrderRepository;
import dev.dini.scms.util.exception.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerOrderServiceImpl implements CustomerOrderService {

    private final CustomerOrderRepository customerOrderRepository;
    private final CustomerOrderMapper customerOrderMapper;
    private final CustomerOrderItemMapper customerOrderItemMapper;
    private final InventoryService inventoryService;
    private final ReservationService reservationService;

    @Override
    @Transactional
    public CustomerOrderResponseDTO createCustomerOrder(CustomerOrderRequestDTO createDTO) {
        log.info("Creating customer order from DTO: {}", createDTO);

        if (!checkInventoryAvailability(createDTO.items())) {
            throw new InsufficientInventoryException("Insufficient inventory for one or more items in the order");
        }

        CustomerOrder order = customerOrderMapper.toEntity(createDTO);
        order.setStatus(CustomerOrderStatus.PENDING);

        List<CustomerOrderItem> orderItems = createOrderItemsFromDTOs(createDTO.items(), order);
        order.setItems(orderItems);

        CustomerOrder savedOrder = customerOrderRepository.save(order);
        reserveInventory(createDTO.items());
        savedOrder.setStatus(CustomerOrderStatus.PROCESSING);
        CustomerOrder updatedOrder = customerOrderRepository.save(savedOrder);

        log.info("Customer order created with status {} and ID {}", updatedOrder.getStatus(), updatedOrder.getId());
        return customerOrderMapper.toResponseDTO(updatedOrder);
    }

    private List<CustomerOrderItem> createOrderItemsFromDTOs(List<CustomerOrderItemRequestDTO> itemDTOs, CustomerOrder order) {
        return itemDTOs.stream()
                .map(itemDTO -> {
                    CustomerOrderItem orderItem = customerOrderItemMapper.toEntity(itemDTO);
                    orderItem.setCustomerOrder(order);
                    return orderItem;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CustomerOrderResponseDTO updateCustomerOrder(Long id, CustomerOrderRequestDTO updateDTO) {
        log.info("Updating customer order with ID {}, using DTO: {}", id, updateDTO);
        CustomerOrder customerOrder = findCustomerOrderById(id);
        customerOrderMapper.partialUpdate(updateDTO, customerOrder);
        CustomerOrder updatedCustomerOrder = customerOrderRepository.save(customerOrder);
        log.info("Customer order updated: {}", updatedCustomerOrder);
        return customerOrderMapper.toResponseDTO(updatedCustomerOrder);
    }

    @Override
    @Transactional
    public void deleteCustomerOrder(Long id) {
        log.info("Deleting customer order with ID: {}", id);
        CustomerOrder order = findCustomerOrderById(id);
        if (order.getStatus() == CustomerOrderStatus.PROCESSING) {
            for (CustomerOrderItem item : order.getItems()) {
                reservationService.releaseReservation(item.getProduct().getId(), item.getQuantity());
                log.info("Released reservation for product ID: {}, quantity: {} during order deletion", item.getProduct().getId(), item.getQuantity());
            }
        }
        customerOrderRepository.deleteById(id);
        log.info("Customer order with ID {} deleted", id);
    }

    @Override
    public CustomerOrderResponseDTO getCustomerOrderById(Long id) {
        log.info("Getting customer order by ID: {}", id);
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
        log.info("Adding item to order ID {}: {}", orderId, itemRequestDTO);
        CustomerOrder order = findCustomerOrderById(orderId);

        // Check inventory availability before adding the item
        if (inventoryService.isInventoryAvailable(itemRequestDTO.productId(), itemRequestDTO.quantity())) {
            throw new InsufficientInventoryException("Insufficient inventory for product ID: " + itemRequestDTO.productId());
        }

        CustomerOrderItem orderItem = customerOrderItemMapper.toEntity(itemRequestDTO);
        orderItem.setCustomerOrder(order);
        order.getItems().add(orderItem);
        CustomerOrder updatedOrder = customerOrderRepository.save(order);

        reservationService.reserveInventory(itemRequestDTO.productId(), itemRequestDTO.quantity());
        log.info("Item added to order ID {}.  Reserved quantity {} of product ID {}", orderId, itemRequestDTO.quantity(), itemRequestDTO.productId());
        return customerOrderMapper.toResponseDTO(updatedOrder);
    }

    @Override
    @Transactional
    public CustomerOrderResponseDTO updateOrderItem(Long orderId, Long itemId, CustomerOrderItemRequestDTO itemRequestDTO) {
        log.info("Updating item ID {} in order ID {} with DTO: {}", itemId, orderId, itemRequestDTO);
        CustomerOrder order = findCustomerOrderById(orderId);

        CustomerOrderItem itemToUpdate = findOrderItemInOrder(order, itemId);

        int quantityDifference = itemRequestDTO.quantity() - itemToUpdate.getQuantity();

        // Check new quantity against available
        if (quantityDifference > 0 && inventoryService.isInventoryAvailable(itemToUpdate.getProduct().getId(), quantityDifference)) {
            throw new InsufficientInventoryException("Insufficient inventory to update order item quantity.");
        }
        itemToUpdate.setQuantity(itemRequestDTO.quantity());
        customerOrderRepository.save(order);

        if (quantityDifference != 0) {
            if (quantityDifference > 0) {
                reservationService.reserveInventory(itemToUpdate.getProduct().getId(), quantityDifference);
                log.info("Reserved {} more units of product ID {} for order item update", quantityDifference, itemToUpdate.getProduct().getId());
            } else {
                reservationService.releaseReservation(itemToUpdate.getProduct().getId(), Math.abs(quantityDifference));
                log.info("Released {} units of product ID {} for order item update", Math.abs(quantityDifference), itemToUpdate.getProduct().getId());
            }
        }
        log.info("Item ID {} in order ID {} updated", itemId, orderId);
        return customerOrderMapper.toResponseDTO(order);
    }

    @Override
    @Transactional
    public CustomerOrderResponseDTO removeItemFromOrder(Long orderId, Long itemId) {
        log.info("Removing item ID {} from order ID {}", itemId, orderId);
        CustomerOrder order = findCustomerOrderById(orderId);

        CustomerOrderItem itemToRemove = findOrderItemInOrder(order, itemId);

        // Release reservation before removing
        reservationService.releaseReservation(itemToRemove.getProduct().getId(), itemToRemove.getQuantity());
        log.info("Released {} units of product ID {} for removed order item", itemToRemove.getQuantity(), itemToRemove.getProduct().getId());

        order.getItems().removeIf(item -> item.getId().equals(itemId));

        CustomerOrder updatedOrder = customerOrderRepository.save(order);
        log.info("Item ID {} removed from order ID {}", itemId, orderId);
        return customerOrderMapper.toResponseDTO(updatedOrder);
    }

    private CustomerOrderItem findOrderItemInOrder(CustomerOrder order, Long itemId) {
        return order.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException(
                        "Order item with id " + itemId + " not found in order " + order.getId()));
    }

    @Override
    public boolean checkInventoryAvailability(List<CustomerOrderItemRequestDTO> items) {
        log.info("Checking inventory availability for {} items", items.size());
        for (CustomerOrderItemRequestDTO item : items) {
            if (inventoryService.isInventoryAvailable(item.productId(), item.quantity())) {
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

        if (order.getStatus() != CustomerOrderStatus.PENDING &&
                order.getStatus() != CustomerOrderStatus.PROCESSING) {
            throw new IllegalStateException("Order cannot be cancelled because it is in " + order.getStatus() + " state");
        }

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

