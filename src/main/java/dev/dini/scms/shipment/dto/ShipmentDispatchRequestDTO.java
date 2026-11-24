package dev.dini.scms.shipment.dto;

import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

/**
 * Request payload for dispatching a pending shipment.
 */
public record ShipmentDispatchRequestDTO(
        @NotBlank(message = "Carrier is required")
        String carrier,
        @NotBlank(message = "Tracking number is required")
        String trackingNumber
) implements Serializable {
}
