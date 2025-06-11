package dev.dini.scms.order.dto;

import dev.dini.scms.order.entity.CustomerOrderStatus;
import dev.dini.scms.util.dto.AddressDTO;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public record CustomerOrderRequestDTO(
        CustomerOrderStatus status,
        AddressDTO shippingAddress,
        List<CustomerOrderItemRequestDTO> items,
        BigDecimal totalCost
) implements Serializable {
}

