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
import dev.dini.scms.util.exception.ShipmentNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentServiceImpl implements ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentMapper shipmentMapper;
    private final CustomerOrderService customerOrderService;

    /**
     * Automatically provisions a PENDING shipment when a customer order is confirmed.
     */
    @EventListener
    @Transactional
    public void onOrderConfirmed(OrderConfirmedEvent event) {
        Long orderId = event.orderId();
        if (shipmentRepository.existsByCustomerOrderId(orderId)) {
            log.info("Shipment already exists for confirmed order ID {}; skipping auto-creation.", orderId);
            return;
        }
        CustomerOrder order = customerOrderService.getEntityById(orderId);
        Shipment shipment = new Shipment();
        shipment.setCustomerOrder(order);
        shipment.setStatus(ShipmentStatus.PENDING);
        Shipment saved = shipmentRepository.save(shipment);
        log.info("Auto-created PENDING shipment ID {} for confirmed order ID {}", saved.getId(), orderId);
    }

    @Override
    @Transactional
    public ShipmentResponseDTO createShipment(ShipmentRequestDTO createDTO) {
        log.info("Creating shipment {}", createDTO);
        if (createDTO.orderId() == null) {
            throw new IllegalArgumentException("Order ID is required to create a shipment.");
        }
        CustomerOrder order = customerOrderService.getEntityById(createDTO.orderId());
        if (order.getStatus() != CustomerOrderStatus.CONFIRMED) {
            throw new IllegalStateException(
                    "Cannot create shipment for order " + order.getId()
                            + " in state " + order.getStatus() + ". Order must be CONFIRMED."
            );
        }

        var shipment = shipmentMapper.toEntity(createDTO);
        shipment.setCustomerOrder(order);
        if (shipment.getStatus() == null) {
            shipment.setStatus(ShipmentStatus.PENDING);
        }

        var savedShipment = shipmentRepository.save(shipment);
        log.info("Shipment created {}", savedShipment);
        return shipmentMapper.toResponseDTO(savedShipment);
    }

    @Override
    @Transactional
    public ShipmentResponseDTO updateShipment(Long id, ShipmentRequestDTO updateDTO) {
        log.info("Updating shipment with id {}: {}", id, updateDTO);
        var shipment = findShipmentById(id);

        if (updateDTO.status() != null && updateDTO.status() != shipment.getStatus()) {
            validateStatusTransition(shipment.getStatus(), updateDTO.status());
        }

        shipmentMapper.partialUpdate(updateDTO, shipment);
        Shipment updatedShipment = shipmentRepository.save(shipment);
        log.info("Shipment updated {}", updatedShipment);
        return shipmentMapper.toResponseDTO(updatedShipment);
    }

    @Override
    @Transactional
    public void deleteShipment(Long id) {
        var shipment = findShipmentById(id);
        if (shipment.getStatus() == ShipmentStatus.SHIPPED || shipment.getStatus() == ShipmentStatus.DELIVERED) {
            throw new IllegalStateException(
                    "Cannot delete shipment " + id + " because it is in " + shipment.getStatus() + " state."
            );
        }
        shipmentRepository.delete(shipment);
    }

    @Override
    public ShipmentResponseDTO getShipment(Long id) {
        var shipment = findShipmentById(id);
        return shipmentMapper.toResponseDTO(shipment);
    }

    @Override
    public List<ShipmentResponseDTO> getAllShipments() {
        return shipmentRepository.findAll().stream()
                .map(shipmentMapper::toResponseDTO)
                .toList();
    }

    @Override
    public List<ShipmentResponseDTO> getShipmentsByOrderId(Long orderId) {
        return shipmentRepository.findByCustomerOrderId(orderId).stream()
                .map(shipmentMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional
    public ShipmentResponseDTO shipShipment(Long id, ShipmentDispatchRequestDTO dispatchDTO) {
        var shipment = findShipmentById(id);
        if (shipment.getStatus() != ShipmentStatus.PENDING) {
            throw new IllegalStateException(
                    "Shipment " + id + " cannot be shipped because it is in " + shipment.getStatus() + " state."
            );
        }

        shipment.setCarrier(dispatchDTO.carrier().trim());
        shipment.setTrackingNumber(dispatchDTO.trackingNumber().trim());
        shipment.setStatus(ShipmentStatus.SHIPPED);
        shipment.setShippedAt(LocalDateTime.now());

        Shipment saved = shipmentRepository.save(shipment);
        customerOrderService.markShipped(saved.getCustomerOrder().getId());
        log.info("Shipment ID {} dispatched via {} ({})", id, saved.getCarrier(), saved.getTrackingNumber());
        return shipmentMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional
    public ShipmentResponseDTO deliverShipment(Long id) {
        var shipment = findShipmentById(id);
        if (shipment.getStatus() != ShipmentStatus.SHIPPED) {
            throw new IllegalStateException(
                    "Shipment " + id + " cannot be marked delivered because it is in " + shipment.getStatus() + " state."
            );
        }

        shipment.setStatus(ShipmentStatus.DELIVERED);
        shipment.setDeliveredAt(LocalDateTime.now());

        Shipment saved = shipmentRepository.save(shipment);
        customerOrderService.markDelivered(saved.getCustomerOrder().getId());
        log.info("Shipment ID {} marked as DELIVERED", id);
        return shipmentMapper.toResponseDTO(saved);
    }

    private void validateStatusTransition(ShipmentStatus current, ShipmentStatus next) {
        boolean valid = switch (current) {
            case PENDING -> next == ShipmentStatus.SHIPPED;
            case SHIPPED -> next == ShipmentStatus.DELIVERED || next == ShipmentStatus.RETURNED;
            case DELIVERED -> next == ShipmentStatus.RETURNED;
            case RETURNED -> false;
        };
        if (!valid) {
            throw new IllegalStateException(
                    "Invalid shipment status transition from " + current + " to " + next
            );
        }
    }

    private Shipment findShipmentById(Long id) {
        return shipmentRepository.findById(id)
                .orElseThrow(() -> new ShipmentNotFoundException(id));
    }
}
