package dev.dini.scms.payment.service;

import dev.dini.scms.payment.dto.PaymentRequestDTO;
import dev.dini.scms.payment.dto.PaymentResponseDTO;
import dev.dini.scms.payment.entity.Payment;

import java.util.Optional;

public interface PaymentService {
    /**
     * Process payment for an order
     * @param paymentRequestDTO payment details
     * @return payment response with transaction details
     */
    PaymentResponseDTO processPayment(PaymentRequestDTO paymentRequestDTO);
    
    /**
     * Get payment details by order id
     * @param orderTd the order id
     * @return optional payment response
     */
    Optional<PaymentResponseDTO> getPaymentByOrderId(Long orderTd);
    
    /**
     * Get payment entity by order id
     * @param orderTd the order id
     * @return optional payment entity
     */
    Optional<Payment> getPaymentEntityByOrderId(Long orderTd);
}
