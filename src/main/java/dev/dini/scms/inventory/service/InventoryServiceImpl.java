package dev.dini.scms.inventory.service;

import dev.dini.scms.inventory.dto.*;
import dev.dini.scms.inventory.entity.Inventory;
import dev.dini.scms.util.InventoryUtil;
import dev.dini.scms.util.exception.*;
import dev.dini.scms.inventory.mapper.InventoryMapper;
import dev.dini.scms.inventory.repository.InventoryRepository;
import dev.dini.scms.product.service.ProductService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryMapper inventoryMapper;
    private final ProductService productService;
    private final InventoryUtil inventoryUtil;
    private final InventoryCalculationService calculationService;

    @Override
    @Transactional
    public InventoryResponseDTO createInventory(InventoryRequestDTO createDTO) {
        log.info("Creating inventory {}", createDTO);
        Inventory inventory = inventoryMapper.toEntity(createDTO);

        // Fetch product details from the product service
        inventory.setProduct(productService.getProductEntityById(createDTO.productId()));

        Inventory savedInventory = inventoryRepository.save(inventory);
        log.info("Inventory created {}", savedInventory);
        return inventoryMapper.toResponseDTO(savedInventory);
    }

    @Override
    @Transactional
    public InventoryResponseDTO updateInventory(Long id, InventoryRequestDTO updateDTO) {
        log.info("Updating inventory with id {}: {}", id, updateDTO);

        // Fetch the existing inventory item
        Inventory inventory = findInventoryById(id);

        if (updateDTO.productId() != null && !updateDTO.productId().equals(inventory.getProduct().getId())) {
            throw new IllegalArgumentException("Cannot change product of existing inventory item.");
        }

        inventoryMapper.updateEntity(updateDTO, inventory);
        Inventory updatedInventory = inventoryRepository.save(inventory);
        log.info("Inventory updated {}", updatedInventory);
        return inventoryMapper.toResponseDTO(updatedInventory);
    }

    @Override
    public void deleteInventory(Long id) {
        log.info("Deleting inventory with id {}", id);
        inventoryRepository.deleteById(id);
    }

    @Override
    public List<InventorySummaryDTO> getAllInventorySummaries() {
        log.info("Fetching all inventory summaries");
        return inventoryRepository.findAll().stream()
                .map(inventoryMapper::toSummaryDTO)
                .toList();
    }

    @Override
    @Transactional
    public void reduceInventoryQuantity(Long productId, int quantity) {
        log.info("Reducing inventory for product ID {} by quantity {}", productId, quantity);

        /*
         * Check if the product ID is valid and if the inventory exists.
         * If not, throw an InventoryNotFoundException.
         */
        Inventory inventory = inventoryUtil.findInventoryByProductId(productId);

        // Check if there are enough inventories
        if (inventory.getQuantity() < quantity) {
            throw new InsufficientInventoryException("Insufficient inventory for product ID: " + productId);
        }

        // Reduce the quantity and save the inventory
        inventory.setQuantity(inventory.getQuantity() - quantity);
        inventoryRepository.save(inventory);

        log.info("Inventory reduced for product ID {}. New quantity: {}", productId, inventory.getQuantity());
    }

    @Override
    @Transactional
    public void saveInventory(Inventory inventory) {
        Inventory savedInventory = inventoryRepository.save(inventory);
        inventoryMapper.toResponseDTO(savedInventory);
    }

    @Override
    public boolean isInventoryAvailable(Long productId, int quantity) {
        log.info("Checking inventory availability for product ID {} with quantity {}", productId, quantity);
        try {
            Inventory inventory = inventoryUtil.findInventoryByProductId(productId);
            Integer availableQuantity = calculationService.getAvailableQuantity(inventory);
            boolean isAvailable = availableQuantity >= quantity;
            log.info("Product ID {} has {} available in stock (total: {}, reserved: {}). Required: {}. Available: {}",
                    productId, availableQuantity, inventory.getQuantity(),
                    inventory.getQuantityReserved() != null ? inventory.getQuantityReserved() : 0, 
                    quantity, isAvailable);
            return !isAvailable;
        } catch (InventoryNotFoundException e) {
            log.warn("Inventory not found for product ID: {}", productId);
            return true;
        }
    }

    private Inventory findInventoryById(Long id) {
        return inventoryRepository.findById(id)
                .orElseThrow(() -> new InventoryNotFoundException(id));
    }

}
