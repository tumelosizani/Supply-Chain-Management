package dev.dini.scms.product.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public record ProductSummaryDTO(
        Long id,
        String name,
        BigDecimal unitPrice,
        String category
) implements Serializable { }
