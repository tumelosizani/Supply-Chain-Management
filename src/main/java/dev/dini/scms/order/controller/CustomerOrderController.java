package dev.dini.scms.order.controller;


import dev.dini.scms.order.dto.*;
import dev.dini.scms.order.service.CustomerOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customer-orders")
@RequiredArgsConstructor
public class CustomerOrderController {

    private final CustomerOrderService orderService;

    @PostMapping
    public ResponseEntity<CustomerOrderResponseDTO> createCustomerOrder(@RequestBody CustomerOrderRequestDTO createDTO) {
        CustomerOrderResponseDTO response = orderService.createCustomerOrder(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{orderId}")
    public ResponseEntity<CustomerOrderResponseDTO> updateCustomerOrder(@PathVariable Long orderId, @RequestBody CustomerOrderRequestDTO updateDTO) {
        CustomerOrderResponseDTO response = orderService.updateCustomerOrder(orderId, updateDTO);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> deleteCustomerOrder(@PathVariable Long orderId) {
        orderService.deleteCustomerOrder(orderId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<CustomerOrderResponseDTO> getCustomerOrderById(@PathVariable Long orderId) {
        CustomerOrderResponseDTO response = orderService.getCustomerOrderById(orderId);
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

    @PostMapping("/{orderId}/confirm")
    public ResponseEntity<CustomerOrderResponseDTO> confirmOrder (@PathVariable Long orderId) {
        CustomerOrderResponseDTO response = orderService.confirmOrder(orderId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<CustomerOrderResponseDTO> cancelOrder (@PathVariable Long orderId) {
        CustomerOrderResponseDTO response = orderService.cancelOrder(orderId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<CustomerOrderResponseDTO>> getAllCustomerOrders() {
        List<CustomerOrderResponseDTO> response = orderService.getAllCustomerOrders();
        return ResponseEntity.ok(response);
    }

}
