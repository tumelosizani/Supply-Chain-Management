package dev.dini.scms.shipment.service;

import dev.dini.scms.shipment.dto.ShipmentDispatchRequestDTO;
import dev.dini.scms.shipment.dto.ShipmentRequestDTO;
import dev.dini.scms.shipment.dto.ShipmentResponseDTO;

import java.util.List;

public interface ShipmentService {
    ShipmentResponseDTO createShipment(ShipmentRequestDTO createDTO);

    ShipmentResponseDTO updateShipment(Long id, ShipmentRequestDTO updateDTO);

    void deleteShipment(Long id);

    ShipmentResponseDTO getShipment(Long id);

    List<ShipmentResponseDTO> getAllShipments();

    List<ShipmentResponseDTO> getShipmentsByOrderId(Long orderId);

    ShipmentResponseDTO shipShipment(Long id, ShipmentDispatchRequestDTO dispatchDTO);

    ShipmentResponseDTO deliverShipment(Long id);
}
