package dev.dini.scms.procurement.controller;

import dev.dini.scms.procurement.dto.*;
import dev.dini.scms.procurement.service.PurchaseOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService orderService;

    @PostMapping
    public ResponseEntity<PurchaseOrderResponseDTO> createPurchaseOrder(@RequestBody PurchaseOrderRequestDTO createDTO) {
        PurchaseOrderResponseDTO response = orderService.createPurchaseOrder(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<PurchaseOrderResponseDTO> updatePurchaseOrder(@PathVariable Long id, @RequestBody PurchaseOrderRequestDTO updateDTO) {
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

}
