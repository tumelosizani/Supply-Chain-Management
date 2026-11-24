package dev.dini.scms.order.service;

import dev.dini.scms.inventory.service.InventoryService;
import dev.dini.scms.inventory.service.availability.InventoryAvailabilityService;
import dev.dini.scms.inventory.service.reserve.ReservationService;
import dev.dini.scms.order.dto.CustomerOrderResponseDTO;
import dev.dini.scms.order.entity.CustomerOrder;
import dev.dini.scms.order.entity.CustomerOrderItem;
import dev.dini.scms.order.entity.CustomerOrderStatus;
import dev.dini.scms.order.event.OrderConfirmedEvent;
import dev.dini.scms.order.mapper.CustomerOrderItemMapper;
import dev.dini.scms.order.mapper.CustomerOrderMapper;
import dev.dini.scms.order.repository.CustomerOrderRepository;
import dev.dini.scms.order.service.calulation.OrderCalculationService;
import dev.dini.scms.product.entity.Product;
import dev.dini.scms.product.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerOrderServiceImplTest {

    @Mock
    private CustomerOrderRepository repository;
    @Mock
    private CustomerOrderMapper customerOrderMapper;
    @Mock
    private CustomerOrderItemMapper customerOrderItemMapper;
    @Mock
    private InventoryService inventoryService;
    @Mock
    private ReservationService reservationService;
    @Mock
    private ProductService productService;
    @Mock
    private InventoryAvailabilityService inventoryAvailabilityService;
    @Mock
    private CustomerOrderValidator customerOrderValidator;
    @Mock
    private OrderCalculationService orderCalculationService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private CustomerOrderServiceImpl orderService;

    private CustomerOrder order;

    @BeforeEach
    void setUp() {
        Product product = new Product();
        product.setId(10L);

        CustomerOrderItem item = new CustomerOrderItem();
        item.setId(1L);
        item.setProduct(product);
        item.setQuantity(3);

        order = new CustomerOrder();
        order.setId(42L);
        order.setStatus(CustomerOrderStatus.PROCESSING);
        order.setItems(List.of(item));
    }

    @Test
    @DisplayName("confirmOrder confirms reservations, sets CONFIRMED, and publishes OrderConfirmedEvent")
    void confirmOrder_publishesOrderConfirmedEvent() {
        when(repository.findById(42L)).thenReturn(Optional.of(order));
        when(repository.save(any(CustomerOrder.class))).thenAnswer(inv -> inv.getArgument(0));
        CustomerOrderResponseDTO dto = new CustomerOrderResponseDTO(
                42L, LocalDateTime.now(), CustomerOrderStatus.CONFIRMED, null, List.of(), BigDecimal.TEN
        );
        when(customerOrderMapper.toResponseDTO(order)).thenReturn(dto);

        CustomerOrderResponseDTO result = orderService.confirmOrder(42L);

        assertThat(result.status()).isEqualTo(CustomerOrderStatus.CONFIRMED);
        assertThat(order.getStatus()).isEqualTo(CustomerOrderStatus.CONFIRMED);
        verify(reservationService).confirmReservation(10L, 3);
        verify(eventPublisher).publishEvent(new OrderConfirmedEvent(42L));
    }

    @Test
    @DisplayName("markShipped transitions CONFIRMED -> SHIPPED")
    void markShipped_transitionsConfirmedToShipped() {
        order.setStatus(CustomerOrderStatus.CONFIRMED);
        when(repository.findById(42L)).thenReturn(Optional.of(order));
        when(repository.save(any(CustomerOrder.class))).thenAnswer(inv -> inv.getArgument(0));
        CustomerOrderResponseDTO dto = new CustomerOrderResponseDTO(
                42L, LocalDateTime.now(), CustomerOrderStatus.SHIPPED, null, List.of(), BigDecimal.TEN
        );
        when(customerOrderMapper.toResponseDTO(order)).thenReturn(dto);

        CustomerOrderResponseDTO result = orderService.markShipped(42L);

        assertThat(result.status()).isEqualTo(CustomerOrderStatus.SHIPPED);
        assertThat(order.getStatus()).isEqualTo(CustomerOrderStatus.SHIPPED);
    }

    @Test
    @DisplayName("markShipped rejects non-CONFIRMED orders")
    void markShipped_rejectsNonConfirmed() {
        order.setStatus(CustomerOrderStatus.PENDING);
        when(repository.findById(42L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.markShipped(42L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("SHIPPED");
    }

    @Test
    @DisplayName("markDelivered transitions SHIPPED -> DELIVERED")
    void markDelivered_transitionsShippedToDelivered() {
        order.setStatus(CustomerOrderStatus.SHIPPED);
        when(repository.findById(42L)).thenReturn(Optional.of(order));
        when(repository.save(any(CustomerOrder.class))).thenAnswer(inv -> inv.getArgument(0));
        CustomerOrderResponseDTO dto = new CustomerOrderResponseDTO(
                42L, LocalDateTime.now(), CustomerOrderStatus.DELIVERED, null, List.of(), BigDecimal.TEN
        );
        when(customerOrderMapper.toResponseDTO(order)).thenReturn(dto);

        CustomerOrderResponseDTO result = orderService.markDelivered(42L);

        assertThat(result.status()).isEqualTo(CustomerOrderStatus.DELIVERED);
        assertThat(order.getStatus()).isEqualTo(CustomerOrderStatus.DELIVERED);
    }

    @Test
    @DisplayName("markDelivered rejects non-SHIPPED orders")
    void markDelivered_rejectsNonShipped() {
        order.setStatus(CustomerOrderStatus.CONFIRMED);
        when(repository.findById(42L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.markDelivered(42L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("DELIVERED");
    }
}
