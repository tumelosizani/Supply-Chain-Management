package dev.dini.scms.supplier.dto;

import dev.dini.scms.util.dto.AddressDTO;
import jakarta.validation.constraints.Email;

import java.io.Serializable;


public record SupplierResponseDTO(
        Long id,
        String name,
        String phoneNumber,
        @Email String email,
        AddressDTO address,
        String website,
        double rating
) implements Serializable { }