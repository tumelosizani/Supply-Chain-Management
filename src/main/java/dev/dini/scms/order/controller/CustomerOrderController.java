package dev.dini.scms.order.controller;


import dev.dini.scms.order.dto.*;
import dev.dini.scms.order.service.CustomerOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customer-orders")
@RequiredArgsConstructor
public class CustomerOrderController {

    private final CustomerOrderService orderService;

    @PostMapping
    public ResponseEntity<CustomerOrderResponseDTO> createPurchaseOrder(@RequestBody CustomerOrderRequestDTO createDTO) {
        CustomerOrderResponseDTO response = orderService.createCustomerOrder(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CustomerOrderResponseDTO> updatePurchaseOrder(@PathVariable Long id, @RequestBody CustomerOrderRequestDTO updateDTO) {
        CustomerOrderResponseDTO response = orderService.updateCustomerOrder(id, updateDTO);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePurchaseOrder(@PathVariable Long id) {
        orderService.deleteCustomerOrder(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerOrderResponseDTO> getPurchaseOrderById(@PathVariable Long id) {
        CustomerOrderResponseDTO response = orderService.getCustomerOrderById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{orderId}/items")
    public ResponseEntity<CustomerOrderResponseDTO> addItemToOrder(
            @PathVariable Long orderId,
            @RequestBody CustomerOrderItemRequestDTO itemRequestDTO) {
        CustomerOrderResponseDTO response = orderService.addItemToOrder(orderId, itemRequestDTO);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{orderId}/items/{itemId}")
    public ResponseEntity<CustomerOrderResponseDTO> updateOrderItem(
            @PathVariable Long orderId,
            @PathVariable Long itemId,
            @RequestBody CustomerOrderItemRequestDTO itemRequestDTO) {
        CustomerOrderResponseDTO response = orderService.updateOrderItem(orderId, itemId, itemRequestDTO);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{orderId}/items/{itemId}")
    public ResponseEntity<CustomerOrderResponseDTO> removeItemFromOrder(
            @PathVariable Long orderId,
            @PathVariable Long itemId) {
        CustomerOrderResponseDTO response = orderService.removeItemFromOrder(orderId, itemId);
        return ResponseEntity.ok(response);
    }

}
