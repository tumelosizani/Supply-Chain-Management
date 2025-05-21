package dev.dini.scms.procurement.controller;

import dev.dini.scms.procurement.dto.*;
import dev.dini.scms.procurement.service.PurchaseOrderItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/purchase-order-items")
@RequiredArgsConstructor
public class PurchaseOrderItemController {

    private final PurchaseOrderItemService orderItemService;


    @PatchMapping("/{id}")
    public ResponseEntity<PurchaseOrderItemResponseDTO> updatePurchaseOrderItem(@PathVariable Long id, @RequestBody PurchaseOrderItemRequestDTO updateDTO) {
        PurchaseOrderItemResponseDTO response = orderItemService.updatePurchaseOrderItem(id, updateDTO);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePurchaseOrderItem(@PathVariable Long id) {
        orderItemService.deletePurchaseOrderItem(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseOrderItemResponseDTO> getPurchaseOrderItemById(@PathVariable Long id) {
        PurchaseOrderItemResponseDTO response = orderItemService.getPurchaseOrderItemById(id);
        return ResponseEntity.ok(response);
    }
}
