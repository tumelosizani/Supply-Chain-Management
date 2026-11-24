package dev.dini.scms.shipment.service;

import dev.dini.scms.order.entity.CustomerOrder;
import dev.dini.scms.order.entity.CustomerOrderStatus;
import dev.dini.scms.order.event.OrderConfirmedEvent;
import dev.dini.scms.order.service.CustomerOrderService;
import dev.dini.scms.shipment.dto.ShipmentDispatchRequestDTO;
import dev.dini.scms.shipment.dto.ShipmentRequestDTO;
import dev.dini.scms.shipment.dto.ShipmentResponseDTO;
import dev.dini.scms.shipment.entity.Shipment;
import dev.dini.scms.shipment.entity.ShipmentStatus;
import dev.dini.scms.shipment.mapper.ShipmentMapper;
import dev.dini.scms.shipment.repository.ShipmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShipmentServiceImplTest {

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private ShipmentMapper shipmentMapper;

    @Mock
    private CustomerOrderService customerOrderService;

    @InjectMocks
    private ShipmentServiceImpl shipmentService;

    private CustomerOrder confirmedOrder;
    private Shipment pendingShipment;

    @BeforeEach
    void setUp() {
        confirmedOrder = new CustomerOrder();
        confirmedOrder.setId(42L);
        confirmedOrder.setStatus(CustomerOrderStatus.CONFIRMED);

        pendingShipment = new Shipment();
        pendingShipment.setId(7L);
        pendingShipment.setCustomerOrder(confirmedOrder);
        pendingShipment.setStatus(ShipmentStatus.PENDING);
    }

    @Test
    @DisplayName("onOrderConfirmed auto-creates a PENDING shipment when none exists")
    void onOrderConfirmed_createsPendingShipment() {
        when(shipmentRepository.existsByCustomerOrderId(42L)).thenReturn(false);
        when(customerOrderService.getEntityById(42L)).thenReturn(confirmedOrder);
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(inv -> {
            Shipment s = inv.getArgument(0);
            s.setId(7L);
            return s;
        });

        shipmentService.onOrderConfirmed(new OrderConfirmedEvent(42L));

        ArgumentCaptor<Shipment> captor = ArgumentCaptor.forClass(Shipment.class);
        verify(shipmentRepository).save(captor.capture());
        assertThat(captor.getValue().getCustomerOrder()).isEqualTo(confirmedOrder);
        assertThat(captor.getValue().getStatus()).isEqualTo(ShipmentStatus.PENDING);
    }

    @Test
    @DisplayName("onOrderConfirmed is idempotent when a shipment already exists")
    void onOrderConfirmed_skipsWhenShipmentExists() {
        when(shipmentRepository.existsByCustomerOrderId(42L)).thenReturn(true);

        shipmentService.onOrderConfirmed(new OrderConfirmedEvent(42L));

        verify(shipmentRepository, never()).save(any());
        verify(customerOrderService, never()).getEntityById(any());
    }

    @Test
    @DisplayName("shipShipment transitions PENDING -> SHIPPED, sets carrier/tracking/timestamp, and marks order SHIPPED")
    void shipShipment_transitionsAndSyncsOrder() {
        when(shipmentRepository.findById(7L)).thenReturn(Optional.of(pendingShipment));
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(inv -> inv.getArgument(0));
        ShipmentResponseDTO expectedDto = new ShipmentResponseDTO(
                7L, 42L, "TRK-123", "DHL", ShipmentStatus.SHIPPED, LocalDateTime.now(), null
        );
        when(shipmentMapper.toResponseDTO(any(Shipment.class))).thenReturn(expectedDto);

        ShipmentResponseDTO result = shipmentService.shipShipment(
                7L, new ShipmentDispatchRequestDTO(" DHL ", " TRK-123 ")
        );

        assertThat(result).isEqualTo(expectedDto);
        assertThat(pendingShipment.getStatus()).isEqualTo(ShipmentStatus.SHIPPED);
        assertThat(pendingShipment.getCarrier()).isEqualTo("DHL");
        assertThat(pendingShipment.getTrackingNumber()).isEqualTo("TRK-123");
        assertThat(pendingShipment.getShippedAt()).isNotNull();
        verify(customerOrderService).markShipped(42L);
    }

    @Test
    @DisplayName("shipShipment rejects non-PENDING shipments")
    void shipShipment_rejectsNonPending() {
        pendingShipment.setStatus(ShipmentStatus.SHIPPED);
        when(shipmentRepository.findById(7L)).thenReturn(Optional.of(pendingShipment));

        assertThatThrownBy(() -> shipmentService.shipShipment(7L, new ShipmentDispatchRequestDTO("DHL", "TRK-1")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("SHIPPED");

        verify(customerOrderService, never()).markShipped(any());
    }

    @Test
    @DisplayName("deliverShipment transitions SHIPPED -> DELIVERED, sets timestamp, and marks order DELIVERED")
    void deliverShipment_transitionsAndSyncsOrder() {
        pendingShipment.setStatus(ShipmentStatus.SHIPPED);
        when(shipmentRepository.findById(7L)).thenReturn(Optional.of(pendingShipment));
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(inv -> inv.getArgument(0));
        ShipmentResponseDTO expectedDto = new ShipmentResponseDTO(
                7L, 42L, "TRK-123", "DHL", ShipmentStatus.DELIVERED, LocalDateTime.now(), LocalDateTime.now()
        );
        when(shipmentMapper.toResponseDTO(any(Shipment.class))).thenReturn(expectedDto);

        ShipmentResponseDTO result = shipmentService.deliverShipment(7L);

        assertThat(result).isEqualTo(expectedDto);
        assertThat(pendingShipment.getStatus()).isEqualTo(ShipmentStatus.DELIVERED);
        assertThat(pendingShipment.getDeliveredAt()).isNotNull();
        verify(customerOrderService).markDelivered(42L);
    }

    @Test
    @DisplayName("deliverShipment rejects non-SHIPPED shipments")
    void deliverShipment_rejectsNonShipped() {
        when(shipmentRepository.findById(7L)).thenReturn(Optional.of(pendingShipment));

        assertThatThrownBy(() -> shipmentService.deliverShipment(7L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PENDING");

        verify(customerOrderService, never()).markDelivered(any());
    }

    @Test
    @DisplayName("createShipment rejects orders that are not CONFIRMED")
    void createShipment_requiresConfirmedOrder() {
        confirmedOrder.setStatus(CustomerOrderStatus.PENDING);
        when(customerOrderService.getEntityById(42L)).thenReturn(confirmedOrder);

        assertThatThrownBy(() -> shipmentService.createShipment(
                new ShipmentRequestDTO(42L, "TRK-1", "UPS", ShipmentStatus.PENDING)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CONFIRMED");
    }

    @Test
    @DisplayName("createShipment rejects non-PENDING status")
    void createShipment_rejectsNonPendingStatus() {
        when(customerOrderService.getEntityById(42L)).thenReturn(confirmedOrder);

        assertThatThrownBy(() -> shipmentService.createShipment(
                new ShipmentRequestDTO(42L, "TRK-1", "UPS", ShipmentStatus.SHIPPED)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PENDING");
    }

    @Test
    @DisplayName("updateShipment rejects SHIPPED or DELIVERED status transitions via PATCH")
    void updateShipment_rejectsFulfillmentTransitions() {
        when(shipmentRepository.findById(7L)).thenReturn(Optional.of(pendingShipment));

        assertThatThrownBy(() -> shipmentService.updateShipment(
                7L, new ShipmentRequestDTO(42L, "TRK-1", "UPS", ShipmentStatus.SHIPPED)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("/ship or /deliver");
    }

    @Test
    @DisplayName("deleteShipment rejects SHIPPED or DELIVERED shipments")
    void deleteShipment_rejectsShippedOrDelivered() {
        pendingShipment.setStatus(ShipmentStatus.SHIPPED);
        when(shipmentRepository.findById(7L)).thenReturn(Optional.of(pendingShipment));

        assertThatThrownBy(() -> shipmentService.deleteShipment(7L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("SHIPPED");

        verify(shipmentRepository, never()).delete(any());
    }
}
