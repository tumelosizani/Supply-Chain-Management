package dev.dini.scms.procurement.service;

import dev.dini.scms.procurement.dto.*;
import dev.dini.scms.procurement.entity.*;
import dev.dini.scms.procurement.mapper.*;
import dev.dini.scms.util.exception.PurchaseOrderNotFoundException;
import dev.dini.scms.procurement.repository.PurchaseOrderRepository;
import dev.dini.scms.supplier.service.SupplierService;
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

    @Override
    @Transactional
    public PurchaseOrderResponseDTO createPurchaseOrder(PurchaseOrderRequestDTO createDTO) {
        log.info("Creating purchase order from DTO: {}", createDTO);

        PurchaseOrder order = purchaseOrderMapper.toEntity(createDTO);
        order.setStatus(PurchaseOrderStatus.PENDING);

        // Fetch supplier directly from the supplier service
        order.setSupplier(supplierService.getEntityById(createDTO.supplierId()));

        List<PurchaseOrderItem> orderItems = createOrderItemsFromDTOs(createDTO.items(), order);
        order.setItems(orderItems);

        PurchaseOrder savedPurchaseOrder = purchaseOrderRepository.save(order);

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
        PurchaseOrder purchaseOrder = findPurchaseOrderById(id);
        purchaseOrderMapper.partialUpdate(updateDTO, purchaseOrder);
        PurchaseOrder updatedPurchaseOrder = purchaseOrderRepository.save(purchaseOrder);

        BigDecimal totalAmount = calculationService.calculateOrderTotalAmount(updatedPurchaseOrder);
        log.info("Purchase order updated with ID {}. Total amount: {}", updatedPurchaseOrder.getId(), totalAmount);

        return purchaseOrderMapper.toResponseDTO(updatedPurchaseOrder);
    }

    @Override
    public void deletePurchaseOrder(Long id) {
        log.info("Deleting purchase order with ID: {}", id);
        purchaseOrderRepository.deleteById(id);
        log.info("Purchase order with ID {} deleted", id);
    }

    @Override
    public PurchaseOrderResponseDTO getPurchaseOrderById(Long id) {
        log.info("Fetching purchase order with ID: {}", id);
        PurchaseOrder purchaseOrder = findPurchaseOrderById(id);
        log.info("Purchase order found with ID: {}", id);
        return purchaseOrderMapper.toResponseDTO(purchaseOrder);
    }


    private PurchaseOrder findPurchaseOrderById(Long id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new PurchaseOrderNotFoundException(id));
    }
}
