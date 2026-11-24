package dev.dini.scms.procurement.dto;

import dev.dini.scms.procurement.entity.PurchaseOrderStatus;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public record PurchaseOrderResponseDTO(
        Long id,
        Long supplierId,
        LocalDateTime orderDate,
        LocalDateTime expectedDeliveryDate,
        PurchaseOrderStatus status,
        List<PurchaseOrderItemResponseDTO> items
) implements Serializable { }