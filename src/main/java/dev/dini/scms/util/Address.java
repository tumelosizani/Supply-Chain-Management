package dev.dini.scms.util;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class Address {
    private String street;
    private String city;
    private String state;
    private String country;
    private String zipCode;
}
