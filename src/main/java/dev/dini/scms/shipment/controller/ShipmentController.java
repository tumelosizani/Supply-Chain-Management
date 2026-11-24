package dev.dini.scms.shipment.controller;

import dev.dini.scms.shipment.dto.ShipmentDispatchRequestDTO;
import dev.dini.scms.shipment.dto.ShipmentRequestDTO;
import dev.dini.scms.shipment.dto.ShipmentResponseDTO;
import dev.dini.scms.shipment.service.ShipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/shipments")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService shipmentService;

    @PostMapping
    public ResponseEntity<ShipmentResponseDTO> createShipment(@Valid @RequestBody ShipmentRequestDTO shipmentRequestDTO) {
        ShipmentResponseDTO response = shipmentService.createShipment(shipmentRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ShipmentResponseDTO> updateShipment(@PathVariable Long id, @Valid @RequestBody ShipmentRequestDTO shipmentRequestDTO) {
        ShipmentResponseDTO response = shipmentService.updateShipment(id, shipmentRequestDTO);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShipment(@PathVariable Long id) {
        shipmentService.deleteShipment(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShipmentResponseDTO> getShipment(@PathVariable Long id) {
        ShipmentResponseDTO response = shipmentService.getShipment(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ShipmentResponseDTO>> getAllShipments() {
        List<ShipmentResponseDTO> response = shipmentService.getAllShipments();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<ShipmentResponseDTO>> getShipmentsByOrder(@PathVariable Long orderId) {
        List<ShipmentResponseDTO> response = shipmentService.getShipmentsByOrderId(orderId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/ship")
    public ResponseEntity<ShipmentResponseDTO> shipShipment(
            @PathVariable Long id,
            @Valid @RequestBody ShipmentDispatchRequestDTO dispatchDTO) {
        ShipmentResponseDTO response = shipmentService.shipShipment(id, dispatchDTO);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/deliver")
    public ResponseEntity<ShipmentResponseDTO> deliverShipment(@PathVariable Long id) {
        ShipmentResponseDTO response = shipmentService.deliverShipment(id);
        return ResponseEntity.ok(response);
    }
}
