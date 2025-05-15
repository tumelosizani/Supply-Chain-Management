package dev.dini.scms.order.dto;

import dev.dini.scms.order.entity.CustomerOrderStatus;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public record CustomerOrderRequestDTO(
        Date orderDate,
        CustomerOrderStatus status,
        String shippingAddress,
        List<CustomerOrderItemRequestDTO> items
) implements Serializable {
}

