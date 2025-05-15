package dev.dini.scms.inventory.entity;

import dev.dini.scms.product.entity.Product;
import dev.dini.scms.util.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "inventories")
public class Inventory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Min(0)
    private Integer quantity = 0;
    
    @Min(0)
    private Integer quantityReserved = 0;


    @NotNull(message = "Warehouse location cannot be null")
    private String warehouseLocation;

}
