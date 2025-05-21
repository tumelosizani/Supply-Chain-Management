package dev.dini.scms.payment.dto;

import dev.dini.scms.payment.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PaymentRequestDTO(
    @NotNull Long orderId,
    @NotNull @Positive BigDecimal amount,
    @NotNull PaymentMethod paymentMethod
) {}
