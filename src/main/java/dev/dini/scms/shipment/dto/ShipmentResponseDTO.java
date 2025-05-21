package dev.dini.scms.shipment.dto;

import dev.dini.scms.shipment.entity.ShipmentStatus;

public record ShipmentResponseDTO(
        Long id,
        Long orderId,
        String trackingNumber,
        String carrier,
        ShipmentStatus status
){
}