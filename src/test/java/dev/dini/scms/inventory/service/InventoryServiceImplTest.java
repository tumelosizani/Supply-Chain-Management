package dev.dini.scms.inventory.service;

import dev.dini.scms.inventory.dto.InventoryResponseDTO;
import dev.dini.scms.inventory.dto.StockUpdateRequestDTO;
import dev.dini.scms.inventory.entity.Inventory;
import dev.dini.scms.inventory.mapper.InventoryMapper;
import dev.dini.scms.inventory.repository.InventoryRepository;
import dev.dini.scms.product.entity.Product;
import dev.dini.scms.product.service.ProductService;
import dev.dini.scms.util.InventoryUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private InventoryMapper inventoryMapper;

    @Mock
    private ProductService productService;

    @Mock
    private InventoryUtil inventoryUtil;

    @Mock
    private InventoryCalculationService calculationService;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    @Test
    @DisplayName("addStock increases physical stock when inventory exists")
    void addStock_existingInventory_increasesQuantity() {
        Product product = new Product();
        product.setId(10L);

        Inventory existing = new Inventory();
        existing.setId(1L);
        existing.setProduct(product);
        existing.setQuantity(20);
        existing.setQuantityReserved(5);

        when(inventoryRepository.findByProductId(10L)).thenReturn(Optional.of(existing));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(inv -> inv.getArgument(0));
        when(inventoryMapper.toResponseDTO(any(Inventory.class)))
                .thenReturn(new InventoryResponseDTO(1L, 10L, 35, "DEFAULT"));

        InventoryResponseDTO result = inventoryService.addStock(new StockUpdateRequestDTO(10L, 15));

        assertThat(existing.getQuantity()).isEqualTo(35);
        verify(inventoryRepository).save(existing);
    }

    @Test
    @DisplayName("addStock creates default inventory if not existing for product")
    void addStock_noExistingInventory_createsDefaultRecord() {
        Product product = new Product();
        product.setId(20L);

        when(inventoryRepository.findByProductId(20L)).thenReturn(Optional.empty());
        when(productService.getEntityById(20L)).thenReturn(product);
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(inv -> inv.getArgument(0));

        inventoryService.addStock(new StockUpdateRequestDTO(20L, 50));

        verify(inventoryRepository).save(argThat(inv ->
                inv.getProduct().getId().equals(20L) &&
                        inv.getQuantity() == 50 &&
                        inv.getQuantityReserved() == 0 &&
                        "DEFAULT".equals(inv.getWarehouseLocation())
        ));
    }

    @Test
    @DisplayName("addStock rejects non-positive quantity")
    void addStock_nonPositiveQuantity_throwsException() {
        assertThatThrownBy(() -> inventoryService.addStock(new StockUpdateRequestDTO(10L, 0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quantity to add must be positive");

        assertThatThrownBy(() -> inventoryService.addStock(new StockUpdateRequestDTO(10L, -5)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quantity to add must be positive");
    }
}
