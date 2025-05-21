package dev.dini.scms.payment.dto;

import dev.dini.scms.payment.enums.PaymentMethod;
import dev.dini.scms.payment.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponseDTO(
    Long id,
    Long orderId,
    BigDecimal amount,
    PaymentStatus status,
    PaymentMethod paymentMethod,
    LocalDateTime paymentDate,
    String transactionReference
) {}
