package dev.dini.scms.inventory.service.availability;

import dev.dini.scms.order.dto.CustomerOrderItemRequestDTO;

import java.util.List;

public interface InventoryAvailabilityService {
    void ensureSufficientInventory(List<CustomerOrderItemRequestDTO> items);
}
