package dev.dini.scms.order.entity;

import dev.dini.scms.shipment.entity.Shipment;
import dev.dini.scms.util.Address;
import dev.dini.scms.util.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "customer_orders")
public class CustomerOrder extends BaseEntity {

    private Date orderDate;

    @Enumerated(EnumType.STRING)
    private CustomerOrderStatus status;

    @Embedded
    private Address shippingAddress;

    @OneToMany(mappedBy = "customerOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CustomerOrderItem> items;

    @OneToMany(mappedBy = "customerOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Shipment> shipments;

}
