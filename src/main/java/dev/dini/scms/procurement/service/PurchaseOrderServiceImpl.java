package dev.dini.scms.procurement.service;

import dev.dini.scms.inventory.dto.StockUpdateRequestDTO;
import dev.dini.scms.inventory.service.InventoryService;
import dev.dini.scms.procurement.dto.*;
import dev.dini.scms.procurement.entity.*;
import dev.dini.scms.procurement.mapper.*;
import dev.dini.scms.supplier.service.SupplierService;
import dev.dini.scms.util.exception.InvalidPurchaseOrderStateException;
import dev.dini.scms.util.exception.PurchaseOrderNotFoundException;
import dev.dini.scms.util.exception.PurchaseOrderReceivingException;
import dev.dini.scms.procurement.repository.PurchaseOrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseOrderServiceImpl implements PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderMapper purchaseOrderMapper;
    private final PurchaseOrderItemMapper purchaseOrderItemMapper;
    private final PurchaseOrderCalculationService calculationService;
    private final SupplierService supplierService;
    private final InventoryService inventoryService;

    @Override
    @Transactional
    public PurchaseOrderResponseDTO createPurchaseOrder(PurchaseOrderRequestDTO createDTO) {
        log.info("Creating purchase order from DTO: {}", createDTO);

        var order = purchaseOrderMapper.toEntity(createDTO);
        order.setStatus(PurchaseOrderStatus.PENDING);

        // Fetch supplier directly from the supplier service
        order.setSupplier(supplierService.getEntityById(createDTO.supplierId()));

        List<PurchaseOrderItem> orderItems = createOrderItemsFromDTOs(createDTO.items(), order);
        order.setItems(orderItems);

        var savedPurchaseOrder = purchaseOrderRepository.save(order);

        // Calculate and log total amount
        BigDecimal totalAmount = calculationService.calculateOrderTotalAmount(savedPurchaseOrder);
        log.info("Purchase order created with ID {}. Total amount: {}", savedPurchaseOrder.getId(), totalAmount);

        return purchaseOrderMapper.toResponseDTO(savedPurchaseOrder);
    }

    private List<PurchaseOrderItem> createOrderItemsFromDTOs(List<PurchaseOrderItemRequestDTO> itemDTOs, PurchaseOrder order) {
        return itemDTOs.stream()
                .map(itemDTO -> {
                    PurchaseOrderItem orderItem = purchaseOrderItemMapper.toEntity(itemDTO);
                    orderItem.setPurchaseOrder(order);
                    return orderItem;
                })
                .toList();
    }

    @Override
    @Transactional
    public PurchaseOrderResponseDTO updatePurchaseOrder(Long id, PurchaseOrderRequestDTO updateDTO) {
        log.info("Updating purchase order with ID: {}", id);

        // Fetch the existing purchase order
        var purchaseOrder = findPurchaseOrderById(id);

        if (purchaseOrder.getStatus().isTerminal()) {
            throw new InvalidPurchaseOrderStateException("Cannot update purchase order in terminal status: " + purchaseOrder.getStatus());
        }

        purchaseOrderMapper.partialUpdate(updateDTO, purchaseOrder);
        var updatedPurchaseOrder = purchaseOrderRepository.save(purchaseOrder);

        BigDecimal totalAmount = calculationService.calculateOrderTotalAmount(updatedPurchaseOrder);
        log.info("Purchase order updated with ID {}. Total amount: {}", updatedPurchaseOrder.getId(), totalAmount);

        return purchaseOrderMapper.toResponseDTO(updatedPurchaseOrder);
    }

    @Override
    @Transactional
    public void deletePurchaseOrder(Long id) {
        log.info("Deleting purchase order with ID: {}", id);
        var purchaseOrder = findPurchaseOrderById(id);
        if (purchaseOrder.getStatus() == PurchaseOrderStatus.PARTIALLY_RECEIVED
                || purchaseOrder.getStatus() == PurchaseOrderStatus.FULLY_RECEIVED) {
            throw new InvalidPurchaseOrderStateException("Cannot delete a purchase order that has received goods (status: "
                    + purchaseOrder.getStatus() + ").");
        }
        purchaseOrderRepository.delete(purchaseOrder);
        log.info("Purchase order with ID {} deleted", id);
    }

    @Override
    public PurchaseOrderResponseDTO getPurchaseOrderById(Long id) {
        log.info("Fetching purchase order with ID: {}", id);
        var purchaseOrder = findPurchaseOrderById(id);
        log.info("Purchase order found with ID: {}", id);
        return purchaseOrderMapper.toResponseDTO(purchaseOrder);
    }

    @Override
    public List<PurchaseOrderResponseDTO> getAllPurchaseOrders() {
        log.info("Fetching all purchase orders");
        List<PurchaseOrder> purchaseOrders = purchaseOrderRepository.findAll();
        log.info("Total purchase orders found: {}", purchaseOrders.size());
        return purchaseOrders.stream()
                .map(purchaseOrderMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional
    public PurchaseOrderResponseDTO updatePurchaseOrderStatus(Long id, PurchaseOrderStatus status) {
        log.info("Updating purchase order status with ID: {} to {}", id, status);

        var purchaseOrder = findPurchaseOrderById(id);
        var currentStatus = purchaseOrder.getStatus();

        if (currentStatus == status) {
            return purchaseOrderMapper.toResponseDTO(purchaseOrder);
        }

        validateStatusTransition(currentStatus, status);

        purchaseOrder.setStatus(status);
        var updatedPurchaseOrder = purchaseOrderRepository.save(purchaseOrder);

        log.info("Purchase order status updated with ID {}. New status: {}", updatedPurchaseOrder.getId(), status);
        return purchaseOrderMapper.toResponseDTO(updatedPurchaseOrder);
    }

    @Override
    @Transactional
    public PurchaseOrderResponseDTO receiveGoods(Long id, GoodsReceiptRequestDTO receiptDTO) {
        log.info("Receiving goods for purchase order ID {}: {}", id, receiptDTO);

        var purchaseOrder = findPurchaseOrderById(id);

        if (!purchaseOrder.getStatus().canReceiveGoods()) {
            throw new InvalidPurchaseOrderStateException(
                    "Cannot receive goods for purchase order " + id + " in status " + purchaseOrder.getStatus() +
                            ". Order must be in APPROVED, SHIPPED, DELIVERED, or PARTIALLY_RECEIVED status."
            );
        }

        if (purchaseOrder.getItems() == null || purchaseOrder.getItems().isEmpty()) {
            throw new InvalidPurchaseOrderStateException("Purchase order " + id + " has no items to receive.");
        }

        for (GoodsReceiptItemDTO receiptItem : receiptDTO.items()) {
            if (receiptItem.quantityReceived() <= 0) {
                throw new PurchaseOrderReceivingException(
                        "Received quantity must be positive. Provided: " + receiptItem.quantityReceived());
            }

            PurchaseOrderItem orderItem = findMatchingItem(purchaseOrder, receiptItem);

            int remaining = orderItem.getRemainingQuantity();
            if (receiptItem.quantityReceived() > remaining) {
                throw new PurchaseOrderReceivingException(
                        "Cannot receive " + receiptItem.quantityReceived() + " units for product ID " +
                                orderItem.getProduct().getId() + ". Only " + remaining +
                                " units remain to be received (ordered: " + orderItem.getQuantity() +
                                ", already received: " + orderItem.getQuantityReceived() + ")."
                );
            }

            // Update item quantity received
            orderItem.setQuantityReceived(orderItem.getQuantityReceived() + receiptItem.quantityReceived());

            // Replenish physical stock in inventory
            inventoryService.addStock(new StockUpdateRequestDTO(
                    orderItem.getProduct().getId(),
                    receiptItem.quantityReceived()
            ));

            log.info("Replenished inventory for product ID {} by {} units. Item progress: {}/{}",
                    orderItem.getProduct().getId(),
                    receiptItem.quantityReceived(),
                    orderItem.getQuantityReceived(),
                    orderItem.getQuantity());
        }

        // Determine updated status
        boolean allFullyReceived = purchaseOrder.getItems().stream()
                .allMatch(PurchaseOrderItem::isFullyReceived);

        PurchaseOrderStatus newStatus = allFullyReceived
                ? PurchaseOrderStatus.FULLY_RECEIVED
                : PurchaseOrderStatus.PARTIALLY_RECEIVED;

        purchaseOrder.setStatus(newStatus);
        var savedPurchaseOrder = purchaseOrderRepository.save(purchaseOrder);

        log.info("Purchase order {} goods receipt completed. New status: {}", id, newStatus);
        return purchaseOrderMapper.toResponseDTO(savedPurchaseOrder);
    }

    private PurchaseOrderItem findMatchingItem(PurchaseOrder purchaseOrder, GoodsReceiptItemDTO receiptItem) {
        if (receiptItem.purchaseOrderItemId() != null) {
            return purchaseOrder.getItems().stream()
                    .filter(item -> receiptItem.purchaseOrderItemId().equals(item.getId()))
                    .findFirst()
                    .orElseThrow(() -> new PurchaseOrderReceivingException(
                            "Purchase order item with ID " + receiptItem.purchaseOrderItemId() +
                                    " does not belong to purchase order " + purchaseOrder.getId()));
        } else if (receiptItem.productId() != null) {
            return purchaseOrder.getItems().stream()
                    .filter(item -> item.getProduct() != null && receiptItem.productId().equals(item.getProduct().getId()))
                    .findFirst()
                    .orElseThrow(() -> new PurchaseOrderReceivingException(
                            "Product with ID " + receiptItem.productId() +
                                    " is not an item in purchase order " + purchaseOrder.getId()));
        } else {
            throw new PurchaseOrderReceivingException("Either purchaseOrderItemId or productId must be provided in receipt item");
        }
    }

    private void validateStatusTransition(PurchaseOrderStatus current, PurchaseOrderStatus next) {
        if (current == PurchaseOrderStatus.CANCELLED) {
            throw new InvalidPurchaseOrderStateException("Cannot change status of a cancelled purchase order.");
        }
        if (current == PurchaseOrderStatus.FULLY_RECEIVED && next != PurchaseOrderStatus.RETURNED) {
            throw new InvalidPurchaseOrderStateException("Cannot change status of a fully received purchase order except to RETURNED.");
        }
        if (current == PurchaseOrderStatus.RETURNED) {
            throw new InvalidPurchaseOrderStateException("Cannot change status of a returned purchase order.");
        }
        if ((next == PurchaseOrderStatus.PARTIALLY_RECEIVED || next == PurchaseOrderStatus.FULLY_RECEIVED)
                && (current == PurchaseOrderStatus.PENDING)) {
            throw new InvalidPurchaseOrderStateException("Cannot transition directly from PENDING to " + next + ". Order must be approved first.");
        }
    }

    private PurchaseOrder findPurchaseOrderById(Long id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new PurchaseOrderNotFoundException(id));
    }

}
