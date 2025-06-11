package dev.dini.scms.order.service;

import dev.dini.scms.order.dto.CustomerOrderItemRequestDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CustomerOrderValidator {

    public void validateItems(List<CustomerOrderItemRequestDTO> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item.");
        }

        for (var item : items) {
            if (item.productId() == null) {
                throw new IllegalArgumentException("Each item must have a product ID.");
            }
            if (item.quantity() <= 0) {
                throw new IllegalArgumentException("Item quantity must be greater than 0. Found: " + item.quantity());
            }
        }
    }
}
