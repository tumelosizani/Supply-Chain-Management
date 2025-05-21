package dev.dini.scms.procurement.dto;

import dev.dini.scms.procurement.entity.PurchaseOrderStatus;

import java.util.Date;
import java.util.List;

public record PurchaseOrderRequestDTO(
        Long supplierId,
        Date orderDate,
        Date expectedDeliveryDate,
        PurchaseOrderStatus status,
        List<PurchaseOrderItemRequestDTO> items
) {
}
