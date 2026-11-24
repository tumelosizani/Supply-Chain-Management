package dev.dini.scms.order.dto;

import dev.dini.scms.order.entity.CustomerOrderStatus;
import dev.dini.scms.util.dto.AddressDTO;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CustomerOrderResponseDTO(
        Long id,
        LocalDateTime orderDate,
        CustomerOrderStatus status,
        AddressDTO shippingAddress,
        List<CustomerOrderItemResponseDTO> items,
        BigDecimal totalCost
) implements Serializable { }
