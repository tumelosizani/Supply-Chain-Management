package dev.dini.scms.shipment.dto;

import dev.dini.scms.shipment.entity.ShipmentStatus;

import java.time.LocalDateTime;

public record ShipmentResponseDTO(
        Long id,
        Long orderId,
        String trackingNumber,
        String carrier,
        ShipmentStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
){
}