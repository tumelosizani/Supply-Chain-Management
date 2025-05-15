package dev.dini.scms.procurement.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public record PurchaseOrderItemRequestDTO(
        Long productId,
        int quantity,
        BigDecimal unitPrice,
        Long purchaseOrderId
) implements Serializable {
}
