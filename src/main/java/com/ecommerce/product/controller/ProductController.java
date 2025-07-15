package com.ecommerce.product.controller;

import com.ecommerce.product.dto.QuantityDto;
import com.ecommerce.product.service.ProductService;
import com.ecommerce.product.dto.ProductRequestDto;
import com.ecommerce.product.dto.ProductResponseDto;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/products")
public class ProductController {
    private final ProductService productService;
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ProductResponseDto> create(@RequestBody @Valid ProductRequestDto dto) {
        logger.info("Create product request {}", dto.toString());
        return ResponseEntity.ok(productService.create(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDto> get(@PathVariable UUID id) {
        logger.info("Get product request {}", id);
        ProductResponseDto product = productService.get(id);
        return ResponseEntity.ok(product);
    }


    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDto> update(@PathVariable UUID id, @RequestBody ProductRequestDto dto) {
        return ResponseEntity.ok(productService.update(id, dto));
    }

    @GetMapping
    public Page<ProductResponseDto> getProducts(@RequestParam(required = false) String name, Pageable pageable) {
        return productService.getProducts(name, pageable);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/dispense")
    public ResponseEntity<ProductResponseDto> dispenseInventory(@PathVariable UUID id, @RequestBody QuantityDto dto) {
        return ResponseEntity.ok(productService.dispense(id, dto.getQuantity()));
    }

    @PostMapping("/{id}/restock")
    public ResponseEntity<ProductResponseDto> restockInventory(@PathVariable UUID id, @RequestBody QuantityDto dto) {
        return ResponseEntity.ok(productService.restock(id, dto.getQuantity()));
    }

}
