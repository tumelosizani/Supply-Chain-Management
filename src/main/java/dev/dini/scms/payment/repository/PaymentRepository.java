package dev.dini.scms.payment.repository;

import dev.dini.scms.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByCustomerOrder_Id(Long orderTd);
}
