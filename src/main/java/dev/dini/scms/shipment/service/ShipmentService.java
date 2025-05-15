package dev.dini.scms.shipment.service;

import dev.dini.scms.shipment.dto.ShipmentRequestDTO;
import dev.dini.scms.shipment.dto.ShipmentResponseDTO;

public interface ShipmentService {
    ShipmentResponseDTO createShipment(ShipmentRequestDTO createDTO);

    ShipmentResponseDTO updateShipment(Long id, ShipmentRequestDTO updateDTO);

    void deleteShipment(Long id);

    ShipmentResponseDTO getShipment(Long id);
}
