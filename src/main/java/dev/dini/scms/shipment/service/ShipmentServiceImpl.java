package dev.dini.scms.shipment.service;

import dev.dini.scms.order.service.CustomerOrderService;
import dev.dini.scms.shipment.dto.ShipmentRequestDTO;
import dev.dini.scms.shipment.dto.ShipmentResponseDTO;
import dev.dini.scms.shipment.entity.Shipment;
import dev.dini.scms.shipment.mapper.ShipmentMapper;
import dev.dini.scms.shipment.repository.ShipmentRepository;
import dev.dini.scms.util.exception.ShipmentNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentServiceImpl implements ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentMapper shipmentMapper;
    private final CustomerOrderService customerOrderService;

    @Override
    @Transactional
    public ShipmentResponseDTO createShipment(ShipmentRequestDTO createDTO) {
        log.info("Creating shipment {}", createDTO);
        Shipment shipment = shipmentMapper.toEntity(createDTO);

        // Fetch the customer order entity to set it in the shipment
        shipment.setCustomerOrder(customerOrderService.getEntityById(createDTO.orderId()));

        Shipment savedShipment = shipmentRepository.save(shipment);
        log.info("Shipment created {}", savedShipment);
        return shipmentMapper.toResponseDTO(savedShipment);
    }

    @Override
    @Transactional
    public ShipmentResponseDTO updateShipment(Long id, ShipmentRequestDTO updateDTO) {
        log.info("Updating shipment with id {}: {}", id, updateDTO);

        // Fetch the existing shipment
        Shipment shipment = findShipmentById(id);

        shipmentMapper.partialUpdate(updateDTO, shipment);
        Shipment updatedShipment = shipmentRepository.save(shipment);
        log.info("Shipment updated {}", updatedShipment);
        return shipmentMapper.toResponseDTO(updatedShipment);
    }

    @Override
    public void deleteShipment(Long id) {
        Shipment shipment = findShipmentById(id);
        shipmentRepository.delete(shipment);
    }

    @Override
    public ShipmentResponseDTO getShipment(Long id) {
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new ShipmentNotFoundException(id));
        return shipmentMapper.toResponseDTO(shipment);
    }

    @Override
    public List<ShipmentResponseDTO> getAllShipments() {
        return shipmentRepository.findAll().stream()
                .map(shipmentMapper::toResponseDTO)
                .toList();
    }

    private Shipment findShipmentById(Long id) {
        return shipmentRepository.findById(id)
                .orElseThrow(() -> new ShipmentNotFoundException(id));
    }
}