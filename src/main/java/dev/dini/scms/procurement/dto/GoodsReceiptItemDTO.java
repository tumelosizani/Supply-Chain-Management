package dev.dini.scms.procurement.dto;

import jakarta.validation.constraints.Min;
import java.io.Serializable;

public record GoodsReceiptItemDTO(
        Long purchaseOrderItemId,
        Long productId,
        @Min(value = 1, message = "Received quantity must be at least 1")
        int quantityReceived
) implements Serializable {
}
