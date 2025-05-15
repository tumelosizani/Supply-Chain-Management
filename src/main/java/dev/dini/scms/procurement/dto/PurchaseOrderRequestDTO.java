package dev.dini.scms.procurement.dto;

import dev.dini.scms.procurement.entity.PurchaseOrderStatus;

import java.util.Date;

public record PurchaseOrderRequestDTO(
        Long supplierId,
        Date date,
        Date expectedDeliveryDate,
        PurchaseOrderStatus status
) {
}
