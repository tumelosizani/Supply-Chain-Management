package dev.dini.scms.procurement.service;

import dev.dini.scms.procurement.dto.PurchaseOrderItemRequestDTO;
import dev.dini.scms.procurement.dto.PurchaseOrderItemResponseDTO;
import dev.dini.scms.procurement.entity.PurchaseOrderItem;
import dev.dini.scms.procurement.mapper.PurchaseOrderItemMapper;
import dev.dini.scms.procurement.repository.PurchaseOrderItemRepository;
import dev.dini.scms.util.exception.InvalidPurchaseOrderStateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseOrderItemServiceImplTest {

    @Mock
    private PurchaseOrderItemRepository purchaseOrderItemRepository;

    @Mock
    private PurchaseOrderItemMapper purchaseOrderItemMapper;

    @InjectMocks
    private PurchaseOrderItemServiceImpl itemService;

    @Test
    @DisplayName("Reducing ordered quantity below received quantity throws IllegalArgumentException")
    void updatePurchaseOrderItem_belowReceivedQuantity_throwsException() {
        PurchaseOrderItem item = new PurchaseOrderItem();
        item.setId(1L);
        item.setQuantity(10);
        item.setQuantityReceived(5);

        when(purchaseOrderItemRepository.findById(1L)).thenReturn(Optional.of(item));

        PurchaseOrderItemRequestDTO updateDTO = new PurchaseOrderItemRequestDTO(
                100L, 4, BigDecimal.TEN, 1L // requesting quantity 4 < 5
        );

        assertThatThrownBy(() -> itemService.updatePurchaseOrderItem(1L, updateDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot reduce ordered quantity below already received quantity");

        verify(purchaseOrderItemRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deleting item that has already received stock throws InvalidPurchaseOrderStateException")
    void deletePurchaseOrderItem_alreadyReceived_throwsException() {
        PurchaseOrderItem item = new PurchaseOrderItem();
        item.setId(1L);
        item.setQuantity(10);
        item.setQuantityReceived(2);

        when(purchaseOrderItemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> itemService.deletePurchaseOrderItem(1L))
                .isInstanceOf(InvalidPurchaseOrderStateException.class)
                .hasMessageContaining("Cannot delete purchase order item that has already received 2 units");

        verify(purchaseOrderItemRepository, never()).delete(any());
    }
}
