package com.ecommerce.product.service;

import com.ecommerce.product.exception.ProductException;
import com.ecommerce.product.model.Product;
import com.ecommerce.product.repository.ProductRepository;
import com.ecommerce.product.dto.ProductRequestDto;
import com.ecommerce.product.dto.ProductResponseDto;
import com.ecommerce.product.mapper.ProductMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductService(ProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    public ProductResponseDto create(ProductRequestDto dto) {
        return productMapper.toDto(productRepository.save(productMapper.toEntity(dto)));
    }

    public Optional<ProductResponseDto> get(UUID id) {
        return productRepository.findById(id).map(productMapper::toDto);
    }

    public ProductResponseDto update(UUID id, ProductRequestDto dto) {
        Product product = productRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Product does not exist"));
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
