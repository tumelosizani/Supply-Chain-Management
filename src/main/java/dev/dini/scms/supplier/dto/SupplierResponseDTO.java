package dev.dini.scms.supplier.dto;

import jakarta.validation.constraints.Email;

import java.io.Serializable;
import java.time.LocalDateTime;


public record SupplierResponseDTO(
        Long id,
        String name,
        String phoneNumber,
        @Email String email,
        AddressDTO address,
        String website,
        double rating,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) implements Serializable { }