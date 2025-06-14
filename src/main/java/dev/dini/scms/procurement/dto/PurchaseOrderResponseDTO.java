package dev.dini.scms.procurement.dto;

import dev.dini.scms.procurement.entity.PurchaseOrderStatus;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public record PurchaseOrderResponseDTO(
        Long id,
        Long supplierId,
        Date orderDate,
        Date expectedDeliveryDate,
        PurchaseOrderStatus status,
        List<PurchaseOrderItemResponseDTO> items
) implements Serializable { }