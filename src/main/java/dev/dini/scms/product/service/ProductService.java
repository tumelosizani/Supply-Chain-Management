package dev.dini.scms.product.service;

import dev.dini.scms.product.entity.Product;
import dev.dini.scms.product.dto.*;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductService {
    ProductResponseDTO createProduct(ProductRequestDTO createDTO);

    ProductResponseDTO updateProduct(Long id, ProductRequestDTO updateDTO);

    void deleteProduct(Long id);

    ProductResponseDTO getProductById(Long id);

    Page<ProductSummaryDTO> getAllProductSummaries(int page, int size, String sortBy, String sortDirection);

    Product getProductEntityById(Long id);

    List<ProductResponseDTO> getProductsByCategory(String category);

    List<ProductResponseDTO> searchProducts(String query);
}

