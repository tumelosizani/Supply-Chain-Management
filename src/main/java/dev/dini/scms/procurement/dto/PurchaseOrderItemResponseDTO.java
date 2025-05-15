package dev.dini.scms.procurement.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public record PurchaseOrderItemResponseDTO(
        Long id,
        Long productId,
        int quantity,
        BigDecimal unitPrice,
        Long purchaseOrderId
) implements Serializable { }