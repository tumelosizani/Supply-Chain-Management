package dev.dini.scms.procurement.service;

import dev.dini.scms.procurement.dto.*;
import dev.dini.scms.procurement.entity.PurchaseOrderItem;
import dev.dini.scms.util.exception.InvalidPurchaseOrderStateException;
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
public class PurchaseOrderItemServiceImpl implements PurchaseOrderItemService {

    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final PurchaseOrderItemMapper purchaseOrderItemMapper;

    @Override
    @Transactional
    public PurchaseOrderItemResponseDTO updatePurchaseOrderItem(Long id, PurchaseOrderItemRequestDTO updateDTO) {
        log.info("Updating purchase order item {}", updateDTO);

        PurchaseOrderItem purchaseOrderItem = findPurchaseOrderItemById(id);

        if (updateDTO.quantity() < purchaseOrderItem.getQuantityReceived()) {
            throw new IllegalArgumentException(
                    "Cannot reduce ordered quantity below already received quantity (" +
                            purchaseOrderItem.getQuantityReceived() + ")."
            );
        }

        purchaseOrderItemMapper.partialUpdate(updateDTO, purchaseOrderItem);

        PurchaseOrderItem updatedPurchaseOrderItem = purchaseOrderItemRepository.save(purchaseOrderItem);
        log.info("Purchase order item updated {}", updatedPurchaseOrderItem);
        return purchaseOrderItemMapper.toResponseDTO(updatedPurchaseOrderItem);
    }

    @Override
    @Transactional
    public void deletePurchaseOrderItem(Long id) {
        log.info("Deleting purchase order item with id {}", id);
        PurchaseOrderItem purchaseOrderItem = findPurchaseOrderItemById(id);
        if (purchaseOrderItem.getQuantityReceived() > 0) {
            throw new InvalidPurchaseOrderStateException(
                    "Cannot delete purchase order item that has already received " +
                            purchaseOrderItem.getQuantityReceived() + " units."
            );
        }
        purchaseOrderItemRepository.delete(purchaseOrderItem);
        log.info("Purchase order item with id {} deleted", id);
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
                .orElseThrow(() -> new PurchaseOrderNotFoundException("Purchase order item not found with id: " + id));
    }
}
