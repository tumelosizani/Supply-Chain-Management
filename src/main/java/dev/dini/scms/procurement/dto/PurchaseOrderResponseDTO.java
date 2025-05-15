package dev.dini.scms.procurement.dto;

import dev.dini.scms.procurement.entity.PurchaseOrderStatus;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

public record PurchaseOrderResponseDTO(
        Long id,
        Long supplierId,
        Date date,
        Date expectedDeliveryDate,
        PurchaseOrderStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) implements Serializable { }