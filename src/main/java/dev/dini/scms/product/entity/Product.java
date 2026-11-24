package dev.dini.scms.product.entity;

import dev.dini.scms.util.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

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

}
