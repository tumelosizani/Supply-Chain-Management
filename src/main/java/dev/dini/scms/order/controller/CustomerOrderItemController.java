package dev.dini.scms.order.controller;


import dev.dini.scms.order.dto.*;
import dev.dini.scms.order.service.CustomerOrderItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customer-order-items")
@RequiredArgsConstructor
public class CustomerOrderItemController {

    private final CustomerOrderItemService orderItemService;


    @PatchMapping("/{id}")
    public ResponseEntity<CustomerOrderItemResponseDTO> updatePurchaseOrderItem(@PathVariable Long id, @RequestBody CustomerOrderItemRequestDTO updateDTO) {
        CustomerOrderItemResponseDTO response = orderItemService.updateCustomerOrderItem(id, updateDTO);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePurchaseOrderItem(@PathVariable Long id) {
        orderItemService.deleteCustomerOrderItem(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerOrderItemResponseDTO> getPurchaseOrderItemById(@PathVariable Long id) {
        CustomerOrderItemResponseDTO response = orderItemService.getCustomerOrderItemById(id);
        return ResponseEntity.ok(response);
    }
}
