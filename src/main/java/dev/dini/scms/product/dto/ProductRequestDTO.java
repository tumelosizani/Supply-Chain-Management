package dev.dini.scms.product.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public record ProductRequestDTO(
        String name,
        String description,
        BigDecimal unitPrice,
        String category
) implements Serializable { }