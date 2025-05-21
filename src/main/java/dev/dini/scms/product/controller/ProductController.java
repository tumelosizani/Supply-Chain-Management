package dev.dini.scms.product.controller;

import dev.dini.scms.product.dto.*;
import dev.dini.scms.product.service.ProductService;
import dev.dini.scms.util.dto.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponseDTO> createProduct(@RequestBody @Valid ProductRequestDTO createDTO) {
        ProductResponseDTO productResponseDTO = productService.createProduct(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(productResponseDTO);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> updateProduct(@PathVariable Long id, @RequestBody @Valid ProductRequestDTO updateDTO) {
        ProductResponseDTO productResponseDTO = productService.updateProduct(id, updateDTO);
        return ResponseEntity.ok(productResponseDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getProductById(@PathVariable Long id) {
        ProductResponseDTO productResponseDTO = productService.getProductById(id);
        return ResponseEntity.ok(productResponseDTO);
    }

    @GetMapping("/summaries")
    public ResponseEntity<PageResponse<ProductSummaryDTO>> getAllProductSummaries(Pageable pageable) {
        PageResponse<ProductSummaryDTO> summaries = productService.getAllProductSummaries(pageable);
        return ResponseEntity.ok(summaries);
    }
}
