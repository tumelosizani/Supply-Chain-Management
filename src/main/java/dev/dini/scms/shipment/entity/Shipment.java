package dev.dini.scms.shipment.entity;

import dev.dini.scms.order.entity.CustomerOrder;
import dev.dini.scms.util.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "shipments")
public class Shipment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private CustomerOrder customerOrder;

    private String trackingNumber;

    private String carrier;

    @Enumerated(EnumType.STRING)
    private ShipmentStatus status;

}
