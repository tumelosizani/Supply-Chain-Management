package dev.dini.scms.order.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public record CustomerOrderItemRequestDTO(
        Long productId,
        int quantity,
        Long customerOrderId,
        BigDecimal price
) implements Serializable {
}
