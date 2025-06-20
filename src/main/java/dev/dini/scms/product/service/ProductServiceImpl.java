package dev.dini.scms.product.service;

import dev.dini.scms.product.dto.*;
import dev.dini.scms.product.entity.Product;
import dev.dini.scms.util.dto.PageResponse;
import dev.dini.scms.util.exception.ProductNotFoundException;
import dev.dini.scms.product.mapper.ProductMapper;
import dev.dini.scms.product.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public ProductResponseDTO createProduct(ProductRequestDTO createDTO) {
        log.info("Creating product {}", createDTO);
        var product = productMapper.toEntity(createDTO);
        var savedProduct = productRepository.save(product);
        log.info("Product created {}", savedProduct);
        return productMapper.toResponseDTO(savedProduct);
    }

    @Override
    @Transactional
    public ProductResponseDTO updateProduct(Long id, ProductRequestDTO updateDTO) {
        log.info("Updating product with id {}: {}", id, updateDTO);

        var product = findProductById(id);
        productMapper.partialUpdate(updateDTO, product);
        var updatedProduct = productRepository.save(product);
        log.info("Product updated {}", updatedProduct);
        return productMapper.toResponseDTO(updatedProduct);
    }
    
    @Override
    public void deleteProduct(Long id) {
        log.info("Deleting product with id {}", id);
        productRepository.deleteById(id);
    }
    
    @Override
    public ProductResponseDTO getProductById(Long id) {
        log.info("Fetching product with id {}", id);
        var product = findProductById(id);
        return productMapper.toResponseDTO(product);
    }

    @Override
    public PageResponse<ProductSummaryDTO> getAllProductSummaries(Pageable pageable) {
        Page<ProductSummaryDTO> page = productRepository.findAll(pageable)
                .map(productMapper::toSummaryDTO);
        return PageResponse.from(page);
    }

    @Override
    public Product getEntityById(Long id) {
        log.info("Fetching product entity with id {}", id);
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }


    private Product findProductById(Long id) {
        log.info("Finding product with id {}", id);
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }
}
