package dev.dini.scms.product.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponseDTO(
        Long id,
        String name,
        String description,
        BigDecimal unitPrice,
        String category,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) implements Serializable {
  }