package dev.dini.scms.supplier.service;

import dev.dini.scms.supplier.dto.SupplierRequestDTO;
import dev.dini.scms.supplier.dto.SupplierResponseDTO;
import dev.dini.scms.supplier.entity.Supplier;
import dev.dini.scms.supplier.mapper.SupplierMapper;
import dev.dini.scms.supplier.repository.SupplierRepository;
import dev.dini.scms.util.exception.SupplierNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements  SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierMapper mapper;

    @Override
    public SupplierResponseDTO createSupplier(SupplierRequestDTO createDTO) {
        log.info("Creating supplier, {}", createDTO);
        var supplier = mapper.toEntity(createDTO);
        var savedSupplier = supplierRepository.save(supplier);
        log.info("Supplier created {}", savedSupplier);
        return mapper.toResponseDTO(savedSupplier);
    }

    @Override
    public SupplierResponseDTO updateSupplier(Long id, SupplierRequestDTO updateDTO) {
        log.info("Updating supplier, {}", updateDTO);
        var supplier = findSupplierId(id);
        mapper.partialUpdate(updateDTO, supplier);
        var updatedSupplier = supplierRepository.save(supplier);
        log.info("Supplier updated {}", updatedSupplier);
        return mapper.toResponseDTO(updatedSupplier);
    }

    @Override
    public SupplierResponseDTO getSupplierById(Long id) {
        var supplier = findSupplierId(id);
        return mapper.toResponseDTO(supplier);
    }

    @Override
    public void deleteSupplier(Long id) {
        log.info("Deleting supplier with id {}", id);
        var supplier = findSupplierId(id);
        supplierRepository.delete(supplier);
    }

    @Override
    public List<SupplierResponseDTO> getAllSuppliers() {
        List<Supplier> suppliers = supplierRepository.findAll();
        return suppliers.stream()
                .map(mapper::toResponseDTO)
                .toList();
    }

    @Override
    public Supplier getEntityById(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new SupplierNotFoundException(id));
    }

    private Supplier findSupplierId(Long id) {
        return  supplierRepository.findById(id)
                .orElseThrow(() -> new SupplierNotFoundException(id));
    }
}
