package dev.dini.scms.supplier.service;

import dev.dini.scms.supplier.dto.SupplierRequestDTO;
import dev.dini.scms.supplier.dto.SupplierResponseDTO;
import dev.dini.scms.supplier.entity.Supplier;

import java.util.List;

public interface SupplierService {
    SupplierResponseDTO createSupplier(SupplierRequestDTO requestDTO);

    SupplierResponseDTO updateSupplier(Long id, SupplierRequestDTO requestDTO);

    SupplierResponseDTO getSupplierById(Long id);

    void deleteSupplier(Long id);

    List<SupplierResponseDTO> getAllSuppliers();

    Supplier getEntityById(Long id);
}
