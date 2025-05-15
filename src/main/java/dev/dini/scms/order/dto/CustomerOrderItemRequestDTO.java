package dev.dini.scms.order.dto;

import java.io.Serializable;

public record CustomerOrderItemRequestDTO(
        Long productId,
        int quantity,
        Long customerOrderId
) implements Serializable {
}
