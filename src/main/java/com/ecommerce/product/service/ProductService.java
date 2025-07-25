package com.ecommerce.product.service;

import com.ecommerce.product.exception.ProductException;
import com.ecommerce.product.model.Product;
import com.ecommerce.product.repository.ProductRepository;
import com.ecommerce.product.dto.ProductRequestDto;
import com.ecommerce.product.dto.ProductResponseDto;
import com.ecommerce.product.mapper.ProductMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class ProductService {
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductService(ProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    public ProductResponseDto create(ProductRequestDto dto) {
        boolean exists = productRepository.existsByNameIgnoreCaseAndVoidedFalse(dto.getName());
        if (exists) {
            logger.error("Product with name {} already exists", dto.getName());
            throw new ProductException("A non-voided product with this name already exists: " + dto.getName());
        }
        return productMapper.toDto(productRepository.save(productMapper.toEntity(dto)));
    }

    public ProductResponseDto get(UUID id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Product does not exist"));
        return productMapper.toDto(product);
    }

    public ProductResponseDto update(UUID id, ProductRequestDto dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Product does not exist"));

        boolean nameConflict = productRepository.existsByNameIgnoreCaseAndVoidedFalse(dto.getName());
        if (nameConflict && !product.getName().equalsIgnoreCase(dto.getName())) {
            logger.error("Product with name {} already exists", dto.getName());
            throw new ProductException("A non-voided product with this name already exists: " + dto.getName());
        }

        productMapper.updateFromDTO(dto, product);
        return productMapper.toDto(productRepository.save(product));
    }

    public Page<ProductResponseDto> getProducts(String name, Pageable pageable) {
        Page<Product> products = (name == null || name.trim().isEmpty())
                ? productRepository.findAll(pageable)
                : productRepository.findByNameContainingIgnoreCase(name, pageable);

        return products.map(productMapper::toDto);
    }

    public void delete(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Product does not exist"));
        product.setVoided(true);
        productRepository.save(product);
    }

    public ProductResponseDto dispense(UUID id, int quantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Product not found"));

        if (product.getQuantity() < quantity) {
            logger.error("Product quantity less than requested quantity {}", quantity);
            throw new ProductException("Not enough inventory");
        }

        product.setQuantity(product.getQuantity() - quantity);
        return productMapper.toDto(productRepository.save(product));
    }

    public ProductResponseDto restock(UUID id, int quantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Product not found"));

        product.setQuantity(product.getQuantity() + quantity);
        return productMapper.toDto(productRepository.save(product));
    }
}
