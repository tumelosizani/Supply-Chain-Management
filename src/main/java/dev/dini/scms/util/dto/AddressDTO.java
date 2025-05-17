package dev.dini.scms.util.dto;

import jakarta.validation.constraints.NotBlank;

public record AddressDTO(
        @NotBlank String street,
        @NotBlank String city,
        String state,
        @NotBlank String country,
        String zipCode
) {}