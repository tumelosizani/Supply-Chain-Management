package dev.dini.scms.order.dto;

import dev.dini.scms.order.entity.CustomerOrderStatus;
import dev.dini.scms.util.dto.AddressDTO;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public record CustomerOrderResponseDTO(
        Long id,
        Date orderDate,
        CustomerOrderStatus status,
        AddressDTO shippingAddress,
        List<CustomerOrderItemResponseDTO> items
) implements Serializable { }
