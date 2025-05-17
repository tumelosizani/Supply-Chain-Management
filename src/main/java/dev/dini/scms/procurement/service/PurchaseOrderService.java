package dev.dini.scms.procurement.service;

import dev.dini.scms.procurement.dto.PurchaseOrderRequestDTO;
import dev.dini.scms.procurement.dto.PurchaseOrderResponseDTO;
import dev.dini.scms.procurement.entity.PurchaseOrder;

public interface PurchaseOrderService {
    PurchaseOrderResponseDTO createPurchaseOrder(PurchaseOrderRequestDTO createDTO);

    PurchaseOrderResponseDTO updatePurchaseOrder(Long id, PurchaseOrderRequestDTO updateDTO);

    void deletePurchaseOrder(Long id);

    PurchaseOrderResponseDTO getPurchaseOrderById(Long id);

}
