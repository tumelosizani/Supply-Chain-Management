package dev.dini.scms.shipment.repository;

import dev.dini.scms.shipment.entity.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    List<Shipment> findByCustomerOrderId(Long orderId);

    boolean existsByCustomerOrderId(Long orderId);
}
