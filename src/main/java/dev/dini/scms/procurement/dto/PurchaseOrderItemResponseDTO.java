package dev.dini.scms.procurement.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public record PurchaseOrderItemResponseDTO(
        Long id,
        Long productId,
        int quantity,
        int quantityReceived,
        BigDecimal unitPrice,
        Long purchaseOrderId
) implements Serializable { }
