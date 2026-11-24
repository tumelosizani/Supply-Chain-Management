package dev.dini.scms.order.service;

import dev.dini.scms.inventory.entity.Inventory;
import dev.dini.scms.inventory.repository.InventoryRepository;
import dev.dini.scms.order.dto.CustomerOrderItemRequestDTO;
import dev.dini.scms.order.dto.CustomerOrderRequestDTO;
import dev.dini.scms.order.dto.CustomerOrderResponseDTO;
import dev.dini.scms.order.entity.CustomerOrderStatus;
import dev.dini.scms.product.entity.Product;
import dev.dini.scms.product.repository.ProductRepository;
import dev.dini.scms.shipment.dto.ShipmentDispatchRequestDTO;
import dev.dini.scms.shipment.dto.ShipmentResponseDTO;
import dev.dini.scms.shipment.entity.Shipment;
import dev.dini.scms.shipment.entity.ShipmentStatus;
import dev.dini.scms.shipment.repository.ShipmentRepository;
import dev.dini.scms.shipment.service.ShipmentService;
import dev.dini.scms.util.dto.AddressDTO;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OrderFulfillmentIntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private CustomerOrderService customerOrderService;

    @Autowired
    private ShipmentService shipmentService;

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Test
    @DisplayName("confirmOrder publishes OrderConfirmedEvent -> auto-provisions PENDING shipment, and ship/deliver sync order status")
    void confirmOrder_autoProvisionsShipmentAndCompletesFulfillment() {
        Product product = new Product();
        product.setName("Integration Test Widget");
        product.setDescription("Widget for fulfillment integration test");
        product.setUnitPrice(new BigDecimal("25.00"));
        product.setCategory("Hardware");
        product = productRepository.save(product);

        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setQuantity(10);
        inventory.setQuantityReserved(0);
        inventory.setWarehouseLocation("WH-TEST");
        inventoryRepository.save(inventory);

        CustomerOrderRequestDTO orderRequest = new CustomerOrderRequestDTO(
                null,
                new AddressDTO("1 Test St", "Cape Town", "WC", "8001", "ZA"),
                List.of(new CustomerOrderItemRequestDTO(product.getId(), 2, null, product.getUnitPrice())),
                null
        );

        CustomerOrderResponseDTO created = customerOrderService.createCustomerOrder(orderRequest);
        assertThat(created.status()).isEqualTo(CustomerOrderStatus.PENDING);

        CustomerOrderResponseDTO processing = customerOrderService.processOrder(created.id());
        assertThat(processing.status()).isEqualTo(CustomerOrderStatus.PROCESSING);

        CustomerOrderResponseDTO confirmed = customerOrderService.confirmOrder(created.id());
        assertThat(confirmed.status()).isEqualTo(CustomerOrderStatus.CONFIRMED);

        List<Shipment> shipments = shipmentRepository.findByCustomerOrderId(created.id());
        assertThat(shipments).hasSize(1);
        Shipment provisioned = shipments.getFirst();
        assertThat(provisioned.getStatus()).isEqualTo(ShipmentStatus.PENDING);

        ShipmentResponseDTO shipped = shipmentService.shipShipment(
                provisioned.getId(),
                new ShipmentDispatchRequestDTO("DHL", "TRK-INT-001")
        );
        assertThat(shipped.status()).isEqualTo(ShipmentStatus.SHIPPED);
        assertThat(shipped.shippedAt()).isNotNull();
        assertThat(customerOrderService.getCustomerOrderById(created.id()).status())
                .isEqualTo(CustomerOrderStatus.SHIPPED);

        ShipmentResponseDTO delivered = shipmentService.deliverShipment(provisioned.getId());
        assertThat(delivered.status()).isEqualTo(ShipmentStatus.DELIVERED);
        assertThat(delivered.deliveredAt()).isNotNull();
        assertThat(customerOrderService.getCustomerOrderById(created.id()).status())
                .isEqualTo(CustomerOrderStatus.DELIVERED);
    }
}
