package dev.dini.scms.order.dto;

import dev.dini.scms.order.entity.CustomerOrderStatus;
import dev.dini.scms.util.Address;
import dev.dini.scms.util.dto.AddressDTO;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public record CustomerOrderRequestDTO(
        Date orderDate,
        CustomerOrderStatus status,
        AddressDTO shippingAddress,
        List<CustomerOrderItemRequestDTO> items
) implements Serializable {
}

