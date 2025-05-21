package dev.dini.scms.product.service;

import dev.dini.scms.product.entity.Product;
import dev.dini.scms.product.dto.*;
import dev.dini.scms.util.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {
    ProductResponseDTO createProduct(ProductRequestDTO createDTO);

    ProductResponseDTO updateProduct(Long id, ProductRequestDTO updateDTO);

    void deleteProduct(Long id);

    ProductResponseDTO getProductById(Long id);

    PageResponse<ProductSummaryDTO> getAllProductSummaries(Pageable pageable);

    Product getEntityById(Long id);


}
