package dev.dini.scms.inventory.controller;

import dev.dini.scms.inventory.dto.*;
import dev.dini.scms.inventory.service.InventoryService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping
    public ResponseEntity<InventoryResponseDTO> createInventory(@RequestBody @Valid InventoryRequestDTO createDTO){
        InventoryResponseDTO inventoryResponseDTO = inventoryService.createInventory(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryResponseDTO);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<InventoryResponseDTO> updateInventory(@PathVariable Long id, @RequestBody @Valid InventoryRequestDTO updateDTO){
        InventoryResponseDTO inventoryResponseDTO = inventoryService.updateInventory(id, updateDTO);
        return ResponseEntity.ok(inventoryResponseDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInventory(@PathVariable Long id){
        log.info("Deleting inventory with id: {}", id);
        inventoryService.deleteInventory(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summaries")
    public ResponseEntity<List<InventorySummaryDTO>> getAllInventorySummaries() {
        List<InventorySummaryDTO> summaries = inventoryService.getAllInventorySummaries();
        return ResponseEntity.ok(summaries);
    }

    @GetMapping("/stock-level/{productId}")
    public ResponseEntity<Integer> getStockLevel(@PathVariable Long productId) {
        try {
            return ResponseEntity.ok(inventoryService.getStockLevel(productId));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/add-stock")
    public ResponseEntity<InventoryResponseDTO> addStock(@RequestBody @Valid StockUpdateRequestDTO request) {
        InventoryResponseDTO inventoryResponseDTO = inventoryService.addStock(request);
        return ResponseEntity.ok(inventoryResponseDTO);
    }
}
