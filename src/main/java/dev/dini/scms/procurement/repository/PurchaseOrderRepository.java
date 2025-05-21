package dev.dini.scms.procurement.repository;

import dev.dini.scms.procurement.entity.PurchaseOrder;
import dev.dini.scms.procurement.entity.PurchaseOrderStatus;
import dev.dini.scms.supplier.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    List<PurchaseOrder> findBySupplier(Supplier supplier);
    List<PurchaseOrder> findByStatus(PurchaseOrderStatus status);
}