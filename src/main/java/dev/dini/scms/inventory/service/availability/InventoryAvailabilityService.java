package dev.dini.scms.inventory.service.availability;

import dev.dini.scms.inventory.dto.InventoryReservationItemDTO;

import java.util.List;

public interface InventoryAvailabilityService {
    void ensureSufficientInventory(List<InventoryReservationItemDTO> items);
}
