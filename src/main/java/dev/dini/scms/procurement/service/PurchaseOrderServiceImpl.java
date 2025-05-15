package dev.dini.scms.procurement.service;

import dev.dini.scms.procurement.dto.*;
import dev.dini.scms.procurement.entity.PurchaseOrder;
import dev.dini.scms.procurement.entity.PurchaseOrderStatus;
import dev.dini.scms.util.exception.PurchaseOrderNotFoundException;
import dev.dini.scms.procurement.mapper.PurchaseOrderMapper;
import dev.dini.scms.procurement.repository.PurchaseOrderRepository;
import dev.dini.scms.supplier.service.SupplierService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseOrderServiceImpl implements  PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderMapper purchaseOrderMapper;
    private final SupplierService supplierService;


    @Override
    @Transactional
    public PurchaseOrderResponseDTO createPurchaseOrder(PurchaseOrderRequestDTO createDTO) {
        log.info("Creating purchase order {}", createDTO);
        PurchaseOrder order = purchaseOrderMapper.toEntity(createDTO);
        order.setStatus(PurchaseOrderStatus.PENDING);

        // Fetch supplier directly from the supplier service
        order.setSupplier(supplierService.getEntityById(createDTO.supplierId()));

        PurchaseOrder savedPurchaseOrder = purchaseOrderRepository.save(order);
        log.info("Purchase order created {}", savedPurchaseOrder);
        return purchaseOrderMapper.toResponseDTO(savedPurchaseOrder);
    }

    @Override
    @Transactional
    public PurchaseOrderResponseDTO updatePurchaseOrder(Long id, PurchaseOrderRequestDTO updateDTO) {
        log.info("Updating purchase order {}", id);

        // Fetch the existing purchase order
        PurchaseOrder purchaseOrder = findPurchaseOrderById(id);

        purchaseOrderMapper.partialUpdate(updateDTO, purchaseOrder);
        PurchaseOrder updatedPurchaseOrder = purchaseOrderRepository.save(purchaseOrder);

        log.info("Purchase order updated {}", updatedPurchaseOrder);
        return purchaseOrderMapper.toResponseDTO(updatedPurchaseOrder);
    }

    @Override
    public void deletePurchaseOrder(Long id) {
        purchaseOrderRepository.deleteById(id);
    }

    @Override
    public PurchaseOrderResponseDTO getPurchaseOrderById(Long id) {
        PurchaseOrder purchaseOrder = findPurchaseOrderById(id);
        return purchaseOrderMapper.toResponseDTO(purchaseOrder);
    }

    @Override
    public PurchaseOrder getEntityById(Long id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new PurchaseOrderNotFoundException(id));
    }

    private PurchaseOrder findPurchaseOrderById(Long id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new PurchaseOrderNotFoundException(id));
    }


}
