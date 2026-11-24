package dev.dini.scms.procurement.controller;

import dev.dini.scms.procurement.dto.*;
import dev.dini.scms.procurement.entity.PurchaseOrderStatus;
import dev.dini.scms.procurement.service.PurchaseOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService orderService;

    @PostMapping
    public ResponseEntity<PurchaseOrderResponseDTO> createPurchaseOrder(@RequestBody @Valid PurchaseOrderRequestDTO createDTO) {
        PurchaseOrderResponseDTO response = orderService.createPurchaseOrder(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<PurchaseOrderResponseDTO> updatePurchaseOrder(@PathVariable Long id, @RequestBody @Valid PurchaseOrderRequestDTO updateDTO) {
        PurchaseOrderResponseDTO response = orderService.updatePurchaseOrder(id, updateDTO);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePurchaseOrder(@PathVariable Long id) {
        orderService.deletePurchaseOrder(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseOrderResponseDTO> getPurchaseOrderById(@PathVariable Long id) {
        PurchaseOrderResponseDTO response = orderService.getPurchaseOrderById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<PurchaseOrderResponseDTO>> getAllPurchaseOrders() {
        List<PurchaseOrderResponseDTO> response = orderService.getAllPurchaseOrders();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<PurchaseOrderResponseDTO> updatePurchaseOrderStatus(@PathVariable Long id, @RequestBody PurchaseOrderStatus status) {
        PurchaseOrderResponseDTO response = orderService.updatePurchaseOrderStatus(id, status);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/receive")
    public ResponseEntity<PurchaseOrderResponseDTO> receiveGoods(
            @PathVariable Long id,
            @RequestBody @Valid GoodsReceiptRequestDTO receiptDTO) {
        PurchaseOrderResponseDTO response = orderService.receiveGoods(id, receiptDTO);
        return ResponseEntity.ok(response);
    }

}
