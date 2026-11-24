package dev.dini.scms.util.config;

import dev.dini.scms.inventory.entity.Inventory;
import dev.dini.scms.inventory.repository.InventoryRepository;
import dev.dini.scms.order.entity.CustomerOrder;
import dev.dini.scms.order.entity.CustomerOrderItem;
import dev.dini.scms.order.entity.CustomerOrderStatus;
import dev.dini.scms.order.repository.CustomerOrderRepository;
import dev.dini.scms.payment.entity.Payment;
import dev.dini.scms.payment.enums.PaymentStatus;
import dev.dini.scms.payment.repository.PaymentRepository;
import dev.dini.scms.procurement.entity.PurchaseOrder;
import dev.dini.scms.procurement.repository.PurchaseOrderRepository;
import dev.dini.scms.product.entity.Product;
import dev.dini.scms.product.repository.ProductRepository;
import dev.dini.scms.shipment.entity.Shipment;
import dev.dini.scms.shipment.repository.ShipmentRepository;
import dev.dini.scms.supplier.entity.Supplier;
import dev.dini.scms.supplier.repository.SupplierRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataSeederTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private InventoryRepository inventoryRepository;
    @Mock
    private SupplierRepository supplierRepository;
    @Mock
    private CustomerOrderRepository customerOrderRepository;
    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;
    @Mock
    private ShipmentRepository shipmentRepository;
    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private DataSeeder dataSeeder;

    @Test
    @DisplayName("run skips seeding when products already exist")
    void run_whenProductsExist_skipsSeeding() {
        when(productRepository.count()).thenReturn(5L);

        dataSeeder.run(new DefaultApplicationArguments());

        verify(productRepository, never()).saveAll(anyList());
        verifyNoInteractions(inventoryRepository, customerOrderRepository, purchaseOrderRepository, shipmentRepository, paymentRepository);
    }

    @Test
    @DisplayName("run skips seeding when suppliers already exist")
    void run_whenSuppliersExist_skipsSeeding() {
        when(productRepository.count()).thenReturn(0L);
        when(supplierRepository.count()).thenReturn(2L);

        dataSeeder.run(new DefaultApplicationArguments());

        verify(productRepository, never()).saveAll(anyList());
        verify(supplierRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("run seeds consistent records preserving inventory reservation and payment workflow invariants")
    @SuppressWarnings("unchecked")
    void run_whenDatabaseEmpty_seedsConsistentDataset() {
        when(productRepository.count()).thenReturn(0L);
        when(supplierRepository.count()).thenReturn(0L);

        when(productRepository.saveAll(anyList())).thenAnswer(inv -> {
            List<Product> list = inv.getArgument(0);
            for (int i = 0; i < list.size(); i++) {
                list.get(i).setId((long) (i + 1));
            }
            return list;
        });
        when(supplierRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
        when(customerOrderRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
        when(inventoryRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
        when(purchaseOrderRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
        when(shipmentRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
        when(paymentRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        LocalDateTime beforeRun = LocalDateTime.now();
        dataSeeder.run(new DefaultApplicationArguments());
        LocalDateTime afterRun = LocalDateTime.now();

        ArgumentCaptor<List<Product>> productsCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List<Inventory>> inventoriesCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List<CustomerOrder>> ordersCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List<PurchaseOrder>> posCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List<Shipment>> shipmentsCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List<Payment>> paymentsCaptor = ArgumentCaptor.forClass(List.class);

        verify(productRepository).saveAll(productsCaptor.capture());
        verify(inventoryRepository).saveAll(inventoriesCaptor.capture());
        verify(customerOrderRepository).saveAll(ordersCaptor.capture());
        verify(purchaseOrderRepository).saveAll(posCaptor.capture());
        verify(shipmentRepository).saveAll(shipmentsCaptor.capture());
        verify(paymentRepository).saveAll(paymentsCaptor.capture());

        List<Product> products = productsCaptor.getValue();
        List<Inventory> inventories = inventoriesCaptor.getValue();
        List<CustomerOrder> orders = ordersCaptor.getValue();
        List<Payment> payments = paymentsCaptor.getValue();

        assertThat(products).hasSize(200);
        assertThat(inventories).hasSize(200);
        assertThat(orders).hasSize(350);
        assertThat(posCaptor.getValue()).hasSize(150);
        assertThat(shipmentsCaptor.getValue()).isNotEmpty();

        // Verify inventory reservation invariant: reserved == sum of PENDING + PROCESSING order items, and physical >= reserved
        Map<Long, Integer> expectedReserved = new HashMap<>();
        for (CustomerOrder order : orders) {
            assertThat(order.getItems()).isNotEmpty();
            if (order.getStatus() == CustomerOrderStatus.PENDING || order.getStatus() == CustomerOrderStatus.PROCESSING) {
                for (CustomerOrderItem item : order.getItems()) {
                    expectedReserved.merge(item.getProduct().getId(), item.getQuantity(), Integer::sum);
                }
            }
        }
        for (Inventory inv : inventories) {
            int expected = expectedReserved.getOrDefault(inv.getProduct().getId(), 0);
            assertThat(inv.getQuantityReserved()).isEqualTo(expected);
            assertThat(inv.getQuantity()).isGreaterThanOrEqualTo(inv.getQuantityReserved());
        }

        // Verify payment invariants: no payments for PENDING/NEW orders, COMPLETED for PROCESSING+, no future dates
        List<PaymentStatus> statuses = new ArrayList<>();
        for (Payment payment : payments) {
            CustomerOrderStatus orderStatus = payment.getCustomerOrder().getStatus();
            assertThat(orderStatus).isNotIn(CustomerOrderStatus.PENDING, CustomerOrderStatus.NEW);
            if (orderStatus == CustomerOrderStatus.PROCESSING
                    || orderStatus == CustomerOrderStatus.CONFIRMED
                    || orderStatus == CustomerOrderStatus.SHIPPED
                    || orderStatus == CustomerOrderStatus.DELIVERED) {
                assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
            }
            assertThat(payment.getPaymentDate()).isBeforeOrEqualTo(afterRun);
            assertThat(payment.getTransactionReference()).startsWith("PAY-" + beforeRun.getYear() + "-");
            statuses.add(payment.getStatus());
        }
        assertThat(statuses).contains(PaymentStatus.COMPLETED);
    }
}
