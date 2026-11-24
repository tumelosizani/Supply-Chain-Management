package dev.dini.scms.procurement.entity;

import dev.dini.scms.product.entity.Product;
import dev.dini.scms.util.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "purchase_order_items")
public class PurchaseOrderItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Min(0)
    private int quantity;

    @Min(0)
    @Column(nullable = false, columnDefinition = "int default 0")
    private int quantityReceived = 0;

    private BigDecimal unitPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    public int getRemainingQuantity() {
        return Math.max(0, quantity - quantityReceived);
    }

    public boolean isFullyReceived() {
        return quantityReceived >= quantity;
    }
}
