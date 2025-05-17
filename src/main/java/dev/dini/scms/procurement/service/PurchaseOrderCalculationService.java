package dev.dini.scms.procurement.service;

import dev.dini.scms.procurement.entity.PurchaseOrder;
import dev.dini.scms.procurement.entity.PurchaseOrderItem;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PurchaseOrderCalculationService {

    /**
     * Calculates the total price of a single purchase order item.
     *
     * @param item The PurchaseOrderItem for which to calculate the total price.
     * @return The total price of the item, or BigDecimal.ZERO if unit price is null or quantity is zero.
     */
    public BigDecimal calculateItemTotalPrice(PurchaseOrderItem item) {
        if (item.getUnitPrice() != null && item.getQuantity() > 0) {
            return item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        }
        return BigDecimal.ZERO;
    }

    /**
     * Calculates the total amount of a purchase order by summing the total price of all its items.
     *
     * @param purchaseOrder The PurchaseOrder for which to calculate the total amount.
     * @return The total amount of the purchase order.
     */
    public BigDecimal calculateOrderTotalAmount(PurchaseOrder purchaseOrder) {
        BigDecimal orderTotal = BigDecimal.ZERO;
        List<PurchaseOrderItem> items = purchaseOrder.getItems();
        if (items != null && !items.isEmpty()) {
            for (PurchaseOrderItem item : items) {
                orderTotal = orderTotal.add(calculateItemTotalPrice(item));
            }
        }
        return orderTotal;
    }
}