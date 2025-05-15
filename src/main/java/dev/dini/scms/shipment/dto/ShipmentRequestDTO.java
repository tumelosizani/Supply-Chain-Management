package dev.dini.scms.shipment.dto;

import dev.dini.scms.shipment.entity.ShipmentStatus;

import java.io.Serializable;

public record ShipmentRequestDTO(
        Long orderId,
        String trackingNumber,
        String carrier,
        ShipmentStatus status
) implements Serializable {
}