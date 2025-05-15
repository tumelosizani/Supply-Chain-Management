package dev.dini.scms.product.entity;

import dev.dini.scms.inventory.entity.Inventory;
import dev.dini.scms.order.entity.CustomerOrderItem;
import dev.dini.scms.util.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "products")
public class Product extends BaseEntity {

    private String name;

    private String description;

    private BigDecimal unitPrice;

    private String category;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Inventory> inventories;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CustomerOrderItem> orderItems;

}
