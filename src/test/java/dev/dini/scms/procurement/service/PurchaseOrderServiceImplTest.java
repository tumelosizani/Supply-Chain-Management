package dev.dini.scms.procurement.service;

import dev.dini.scms.inventory.dto.InventoryResponseDTO;
import dev.dini.scms.inventory.dto.StockUpdateRequestDTO;
import dev.dini.scms.inventory.service.InventoryService;
import dev.dini.scms.procurement.dto.GoodsReceiptItemDTO;
import dev.dini.scms.procurement.dto.GoodsReceiptRequestDTO;
import dev.dini.scms.procurement.dto.PurchaseOrderResponseDTO;
import dev.dini.scms.procurement.entity.PurchaseOrder;
import dev.dini.scms.procurement.entity.PurchaseOrderItem;
import dev.dini.scms.procurement.entity.PurchaseOrderStatus;
import dev.dini.scms.procurement.mapper.PurchaseOrderItemMapper;
import dev.dini.scms.procurement.mapper.PurchaseOrderMapper;
import dev.dini.scms.procurement.repository.PurchaseOrderRepository;
import dev.dini.scms.product.entity.Product;
import dev.dini.scms.supplier.service.SupplierService;
import dev.dini.scms.util.exception.InvalidPurchaseOrderStateException;
import dev.dini.scms.util.exception.PurchaseOrderReceivingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseOrderServiceImplTest {

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @Mock
    private PurchaseOrderMapper purchaseOrderMapper;

    @Mock
    private PurchaseOrderItemMapper purchaseOrderItemMapper;

    @Mock
    private PurchaseOrderCalculationService calculationService;

    @Mock
    private SupplierService supplierService;

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private PurchaseOrderServiceImpl purchaseOrderService;

    private PurchaseOrder purchaseOrder;
    private PurchaseOrderItem item1;
    private PurchaseOrderItem item2;
    private Product product1;
    private Product product2;

    @BeforeEach
    void setUp() {
        product1 = new Product();
        product1.setId(101L);
        product1.setName("Widget A");

        product2 = new Product();
        product2.setId(102L);
        product2.setName("Widget B");

        item1 = new PurchaseOrderItem();
        item1.setId(1L);
        item1.setProduct(product1);
        item1.setQuantity(10);
        item1.setQuantityReceived(0);
        item1.setUnitPrice(BigDecimal.valueOf(15.00));

        item2 = new PurchaseOrderItem();
        item2.setId(2L);
        item2.setProduct(product2);
        item2.setQuantity(5);
        item2.setQuantityReceived(0);
        item2.setUnitPrice(BigDecimal.valueOf(25.00));

        purchaseOrder = new PurchaseOrder();
        purchaseOrder.setId(1L);
        purchaseOrder.setStatus(PurchaseOrderStatus.APPROVED);
        purchaseOrder.setItems(new ArrayList<>(List.of(item1, item2)));

        item1.setPurchaseOrder(purchaseOrder);
        item2.setPurchaseOrder(purchaseOrder);
    }

    @Test
    @DisplayName("Partial receipt updates item quantities, replenishes inventory, and sets PARTIALLY_RECEIVED status")
    void receiveGoods_partialReceipt_updatesStatusAndInventory() {
        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(purchaseOrder));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(purchaseOrderMapper.toResponseDTO(any(PurchaseOrder.class)))
                .thenReturn(new PurchaseOrderResponseDTO(1L, 1L, null, null, PurchaseOrderStatus.PARTIALLY_RECEIVED, List.of()));

        GoodsReceiptRequestDTO receiptDTO = new GoodsReceiptRequestDTO(
                List.of(new GoodsReceiptItemDTO(1L, null, 6)),
                "Partial shipment received"
        );

        PurchaseOrderResponseDTO result = purchaseOrderService.receiveGoods(1L, receiptDTO);

        assertThat(item1.getQuantityReceived()).isEqualTo(6);
        assertThat(item1.getRemainingQuantity()).isEqualTo(4);
        assertThat(item2.getQuantityReceived()).isEqualTo(0);
        assertThat(purchaseOrder.getStatus()).isEqualTo(PurchaseOrderStatus.PARTIALLY_RECEIVED);

        ArgumentCaptor<StockUpdateRequestDTO> captor = ArgumentCaptor.forClass(StockUpdateRequestDTO.class);
        verify(inventoryService).addStock(captor.capture());
        assertThat(captor.getValue().productId()).isEqualTo(101L);
        assertThat(captor.getValue().quantity()).isEqualTo(6);
    }

    @Test
    @DisplayName("Full receipt of all items sets FULLY_RECEIVED status")
    void receiveGoods_fullReceipt_setsFullyReceivedStatus() {
        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(purchaseOrder));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(purchaseOrderMapper.toResponseDTO(any(PurchaseOrder.class)))
                .thenReturn(new PurchaseOrderResponseDTO(1L, 1L, null, null, PurchaseOrderStatus.FULLY_RECEIVED, List.of()));

        GoodsReceiptRequestDTO receiptDTO = new GoodsReceiptRequestDTO(
                List.of(
                        new GoodsReceiptItemDTO(null, 101L, 10),
                        new GoodsReceiptItemDTO(null, 102L, 5)
                ),
                "All items delivered"
        );

        purchaseOrderService.receiveGoods(1L, receiptDTO);

        assertThat(item1.getQuantityReceived()).isEqualTo(10);
        assertThat(item1.isFullyReceived()).isTrue();
        assertThat(item2.getQuantityReceived()).isEqualTo(5);
        assertThat(item2.isFullyReceived()).isTrue();
        assertThat(purchaseOrder.getStatus()).isEqualTo(PurchaseOrderStatus.FULLY_RECEIVED);

        verify(inventoryService).addStock(new StockUpdateRequestDTO(101L, 10));
        verify(inventoryService).addStock(new StockUpdateRequestDTO(102L, 5));
    }

    @Test
    @DisplayName("Multiple receipts incrementally reach FULLY_RECEIVED")
    void receiveGoods_multipleReceipts_reachesFullyReceived() {
        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(purchaseOrder));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // First receipt: item1 5 units
        purchaseOrderService.receiveGoods(1L, new GoodsReceiptRequestDTO(
                List.of(new GoodsReceiptItemDTO(1L, null, 5)), null));
        assertThat(purchaseOrder.getStatus()).isEqualTo(PurchaseOrderStatus.PARTIALLY_RECEIVED);

        // Second receipt: item1 remaining 5 units, item2 all 5 units
        purchaseOrderService.receiveGoods(1L, new GoodsReceiptRequestDTO(
                List.of(
                        new GoodsReceiptItemDTO(1L, null, 5),
                        new GoodsReceiptItemDTO(2L, null, 5)
                ), null));

        assertThat(item1.getQuantityReceived()).isEqualTo(10);
        assertThat(item2.getQuantityReceived()).isEqualTo(5);
        assertThat(purchaseOrder.getStatus()).isEqualTo(PurchaseOrderStatus.FULLY_RECEIVED);
    }

    @Test
    @DisplayName("Receiving more than remaining quantity throws PurchaseOrderReceivingException")
    void receiveGoods_overReceiving_throwsException() {
        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(purchaseOrder));

        GoodsReceiptRequestDTO receiptDTO = new GoodsReceiptRequestDTO(
                List.of(new GoodsReceiptItemDTO(1L, null, 11)), // ordered 10
                null
        );

        assertThatThrownBy(() -> purchaseOrderService.receiveGoods(1L, receiptDTO))
                .isInstanceOf(PurchaseOrderReceivingException.class)
                .hasMessageContaining("Only 10 units remain to be received");

        verifyNoInteractions(inventoryService);
    }

    @Test
    @DisplayName("Receiving goods on unapproved (PENDING) order throws InvalidPurchaseOrderStateException")
    void receiveGoods_pendingOrder_throwsException() {
        purchaseOrder.setStatus(PurchaseOrderStatus.PENDING);
        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(purchaseOrder));

        GoodsReceiptRequestDTO receiptDTO = new GoodsReceiptRequestDTO(
                List.of(new GoodsReceiptItemDTO(1L, null, 5)), null
        );

        assertThatThrownBy(() -> purchaseOrderService.receiveGoods(1L, receiptDTO))
                .isInstanceOf(InvalidPurchaseOrderStateException.class)
                .hasMessageContaining("Cannot receive goods for purchase order");

        verifyNoInteractions(inventoryService);
    }

    @Test
    @DisplayName("Receiving non-existent item throws PurchaseOrderReceivingException")
    void receiveGoods_itemNotFound_throwsException() {
        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(purchaseOrder));

        GoodsReceiptRequestDTO receiptDTO = new GoodsReceiptRequestDTO(
                List.of(new GoodsReceiptItemDTO(999L, null, 1)), null
        );

        assertThatThrownBy(() -> purchaseOrderService.receiveGoods(1L, receiptDTO))
                .isInstanceOf(PurchaseOrderReceivingException.class)
                .hasMessageContaining("does not belong to purchase order");

        verifyNoInteractions(inventoryService);
    }

    @Test
    @DisplayName("Deleting order with received goods throws InvalidPurchaseOrderStateException")
    void deletePurchaseOrder_partiallyReceived_throwsException() {
        purchaseOrder.setStatus(PurchaseOrderStatus.PARTIALLY_RECEIVED);
        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(purchaseOrder));

        assertThatThrownBy(() -> purchaseOrderService.deletePurchaseOrder(1L))
                .isInstanceOf(InvalidPurchaseOrderStateException.class)
                .hasMessageContaining("Cannot delete a purchase order that has received goods");

        verify(purchaseOrderRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Status transition from CANCELLED throws InvalidPurchaseOrderStateException")
    void updatePurchaseOrderStatus_fromCancelled_throwsException() {
        purchaseOrder.setStatus(PurchaseOrderStatus.CANCELLED);
        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(purchaseOrder));

        assertThatThrownBy(() -> purchaseOrderService.updatePurchaseOrderStatus(1L, PurchaseOrderStatus.APPROVED))
                .isInstanceOf(InvalidPurchaseOrderStateException.class)
                .hasMessageContaining("Cannot change status of a cancelled purchase order");
    }

    @Test
    @DisplayName("Status transition from PENDING to FULLY_RECEIVED directly throws InvalidPurchaseOrderStateException")
    void updatePurchaseOrderStatus_pendingToFullyReceived_throwsException() {
        purchaseOrder.setStatus(PurchaseOrderStatus.PENDING);
        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(purchaseOrder));

        assertThatThrownBy(() -> purchaseOrderService.updatePurchaseOrderStatus(1L, PurchaseOrderStatus.FULLY_RECEIVED))
                .isInstanceOf(InvalidPurchaseOrderStateException.class)
                .hasMessageContaining("Order must be approved first");
    }
}
