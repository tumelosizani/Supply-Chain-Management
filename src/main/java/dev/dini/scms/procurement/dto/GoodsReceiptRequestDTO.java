package dev.dini.scms.procurement.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

public record GoodsReceiptRequestDTO(
        @NotEmpty(message = "Items to receive cannot be empty")
        List<@Valid GoodsReceiptItemDTO> items,
        String notes
) implements Serializable {
}
