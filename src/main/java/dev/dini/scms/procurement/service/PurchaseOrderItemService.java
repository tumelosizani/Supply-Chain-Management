package dev.dini.scms.procurement.service;

import dev.dini.scms.procurement.dto.*;

public interface PurchaseOrderItemService {
    PurchaseOrderItemResponseDTO createPurchaseOrderItem(PurchaseOrderItemRequestDTO createDTO);

    PurchaseOrderItemResponseDTO updatePurchaseOrderItem(Long id, PurchaseOrderItemRequestDTO updateDTO);

    void deletePurchaseOrderItem(Long id);

    PurchaseOrderItemResponseDTO getPurchaseOrderItemById(Long id);
}
