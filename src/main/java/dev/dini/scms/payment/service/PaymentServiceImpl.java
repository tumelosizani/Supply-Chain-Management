package dev.dini.scms.payment.service;

import dev.dini.scms.order.entity.CustomerOrder;
import dev.dini.scms.order.service.CustomerOrderService;
import dev.dini.scms.payment.dto.PaymentRequestDTO;
import dev.dini.scms.payment.dto.PaymentResponseDTO;
import dev.dini.scms.payment.entity.Payment;
import dev.dini.scms.payment.enums.PaymentStatus;
import dev.dini.scms.payment.mapper.PaymentMapper;
import dev.dini.scms.payment.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final CustomerOrderService customerOrderService;

    @Override
    @Transactional
    public PaymentResponseDTO processPayment(PaymentRequestDTO paymentRequestDTO) {
        log.info("Processing payment for order ID: {}, amount: {}", 
                 paymentRequestDTO.orderId(), paymentRequestDTO.amount());
                 
        // Get the order
        CustomerOrder order = customerOrderService.getEntityById(paymentRequestDTO.orderId());

        Payment payment = paymentMapper.toEntity(paymentRequestDTO);

        payment.setAmount(paymentRequestDTO.amount());
        payment.setPaymentMethod(paymentRequestDTO.paymentMethod());
        payment.setPaymentDate(LocalDateTime.now());
        payment.setCustomerOrder(order);
        
        // Generate a dummy transaction reference
        payment.setTransactionReference(generateTransactionReference());
        
        // This is a dummy implementation so we always succeed
        payment.setStatus(PaymentStatus.COMPLETED);
        
        Payment savedPayment = paymentRepository.save(payment);
        log.info("Payment completed for order ID: {}, transaction reference: {}", 
                 order.getId(), savedPayment.getTransactionReference());
                 
        // Update order to PROCESSING since payment is complete
        customerOrderService.processOrder(order.getId());
        
        return paymentMapper.toResponseDTO(savedPayment);
    }
    
    @Override
    public Optional<PaymentResponseDTO> getPaymentByOrderId(Long orderTd) {
        log.info("Getting payment for order ID: {}", orderTd);
        return paymentRepository.findByCustomerOrder_Id(orderTd)
            .map(this::mapToResponseDTO);
    }
    
    @Override
    public Optional<Payment> getPaymentEntityByOrderId(Long orderTd) {
        return paymentRepository.findByCustomerOrder_Id(orderTd);
    }
    
    private String generateTransactionReference() {
        // Generate a unique transaction reference (just a UUID in this dummy implementation)
        return "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    private PaymentResponseDTO mapToResponseDTO(Payment payment) {
        return new PaymentResponseDTO(
            payment.getId(),
            payment.getCustomerOrder().getId(),
            payment.getAmount(),
            payment.getStatus(),
            payment.getPaymentMethod(),
            payment.getPaymentDate(),
            payment.getTransactionReference()
        );
    }
}
