package dev.dini.scms.payment.controller;

import dev.dini.scms.payment.dto.PaymentRequestDTO;
import dev.dini.scms.payment.dto.PaymentResponseDTO;
import dev.dini.scms.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    
    @PostMapping("/process")
    public ResponseEntity<PaymentResponseDTO> processPayment(@RequestBody @Valid PaymentRequestDTO paymentRequestDTO) {
        PaymentResponseDTO response = paymentService.processPayment(paymentRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponseDTO> getPaymentByOrderId(@PathVariable Long orderId) {
        return paymentService.getPaymentByOrderId(orderId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
