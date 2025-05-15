package dev.dini.scms.supplier.dto;

import jakarta.validation.constraints.NotBlank;

public record AddressDTO(
        @NotBlank String street,
        @NotBlank String city,
        String state,
        @NotBlank String country,
        String zipCode
) {}