package dev.dini.scms.procurement.dto;

import dev.dini.scms.procurement.entity.PurchaseOrderStatus;

import java.time.LocalDateTime;
import java.util.List;

public record PurchaseOrderRequestDTO(
        Long supplierId,
        LocalDateTime orderDate,
        LocalDateTime expectedDeliveryDate,
        PurchaseOrderStatus status,
        List<PurchaseOrderItemRequestDTO> items
) {
}
