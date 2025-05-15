package dev.dini.scms.order.dto;

import dev.dini.scms.order.entity.CustomerOrderStatus;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public record CustomerOrderResponseDTO(
        Long id,
        Date orderDate,
        CustomerOrderStatus status,
        String shippingAddress,
        List<CustomerOrderItemResponseDTO> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) implements Serializable { }
