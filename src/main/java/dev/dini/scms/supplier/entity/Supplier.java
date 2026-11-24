package dev.dini.scms.supplier.entity;

import dev.dini.scms.util.Address;
import dev.dini.scms.util.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "suppliers")
public class Supplier extends BaseEntity {

    private String name;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Email
    private String email;

    @Embedded
    private Address address;

    @Column(name = "website")
    private String website;

    private double rating;

}
