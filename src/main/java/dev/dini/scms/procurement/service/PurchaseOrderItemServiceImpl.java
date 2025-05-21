package dev.dini.scms.procurement.service;

import dev.dini.scms.procurement.dto.*;
import dev.dini.scms.procurement.entity.PurchaseOrderItem;
import dev.dini.scms.util.exception.PurchaseOrderNotFoundException;
import dev.dini.scms.procurement.mapper.PurchaseOrderItemMapper;
import dev.dini.scms.procurement.repository.PurchaseOrderItemRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseOrderItemServiceImpl implements  PurchaseOrderItemService {

    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final PurchaseOrderItemMapper purchaseOrderItemMapper;



    @Override
    @Transactional
    public PurchaseOrderItemResponseDTO updatePurchaseOrderItem(Long id, PurchaseOrderItemRequestDTO updateDTO) {
        log.info("Updating purchase order item {}", updateDTO);

        // Fetch the existing purchase order item
        PurchaseOrderItem purchaseOrderItem = findPurchaseOrderItemById(id);

        purchaseOrderItemMapper.partialUpdate(updateDTO, purchaseOrderItem);

        PurchaseOrderItem updatedPurchaseOrderItem = purchaseOrderItemRepository.save(purchaseOrderItem);
        log.info("Purchase order item updated {}", updatedPurchaseOrderItem);
        return purchaseOrderItemMapper.toResponseDTO(updatedPurchaseOrderItem);
    }

    @Override
    public void deletePurchaseOrderItem(Long id) {
        purchaseOrderItemRepository.deleteById(id);
    }

    @Override
    public PurchaseOrderItemResponseDTO getPurchaseOrderItemById(Long id) {
        log.info("Fetching purchase order item with id {}", id);

        PurchaseOrderItem purchaseOrderItem = findPurchaseOrderItemById(id);
        log.info("Purchase order item found {}", purchaseOrderItem);
        return purchaseOrderItemMapper.toResponseDTO(purchaseOrderItem);
    }


    private PurchaseOrderItem findPurchaseOrderItemById(Long id) {
        return purchaseOrderItemRepository.findById(id)
                .orElseThrow(() -> new PurchaseOrderNotFoundException(id));
    }
}