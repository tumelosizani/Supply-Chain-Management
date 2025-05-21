package dev.dini.scms.payment.entity;

import dev.dini.scms.order.entity.CustomerOrder;
import dev.dini.scms.payment.enums.PaymentMethod;
import dev.dini.scms.payment.enums.PaymentStatus;
import dev.dini.scms.util.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payments")
public class Payment extends BaseEntity {

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @Column(nullable = false)
    private LocalDateTime paymentDate;

    private String transactionReference;

    @OneToOne
    @JoinColumn(name = "customer_order_id")
    private CustomerOrder customerOrder;
}
