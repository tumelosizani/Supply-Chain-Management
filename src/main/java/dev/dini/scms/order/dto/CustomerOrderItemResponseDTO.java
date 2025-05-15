package dev.dini.scms.order.dto;

import java.io.Serializable;

public record CustomerOrderItemResponseDTO(
        Long id,
        Long productId,
        int quantity,
        Long customerOrderId
) implements Serializable { }