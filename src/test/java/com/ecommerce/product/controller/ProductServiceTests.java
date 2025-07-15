package com.ecommerce.product.controller;

import com.ecommerce.product.dto.ProductRequestDto;
import com.ecommerce.product.dto.ProductResponseDto;
import com.ecommerce.product.exception.ProductException;
import com.ecommerce.product.mapper.ProductMapper;
import com.ecommerce.product.model.Product;
import com.ecommerce.product.repository.ProductRepository;
import com.ecommerce.product.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ProductServiceTests {
    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private ProductRequestDto requestDto;
    private ProductResponseDto responseDto;
    private UUID productId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        productId = UUID.randomUUID();
        product = new Product();
        product.setId(productId);
        product.setName("Test Product");
        product.setQuantity(10);
        product.setVoided(false);

        requestDto = new ProductRequestDto();
        requestDto.setName("Test Product");
        requestDto.setQuantity(10);

        responseDto = new ProductResponseDto();
        responseDto.setId(productId);
        responseDto.setName("Test Product");
        responseDto.setQuantity(10);
    }

    @Test
    void create_shouldReturnCreatedProduct() {
        when(productMapper.toEntity(requestDto)).thenReturn(product);
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toDto(product)).thenReturn(responseDto);

        ProductResponseDto result = productService.create(requestDto);

        assertEquals(responseDto, result);
        verify(productRepository).save(product);
    }

    @Test
    void get_existingId_shouldReturnProduct() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productMapper.toDto(product)).thenReturn(responseDto);

        Optional<ProductResponseDto> result = productService.get(productId);

        assertTrue(result.isPresent());
        assertEquals(responseDto, result.get());
    }

    @Test
    void get_nonexistentId_shouldReturnEmptyOptional() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        Optional<ProductResponseDto> result = productService.get(productId);

        assertTrue(result.isEmpty());
    }

    @Test
    void update_existingProduct_shouldApplyChanges() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        doNothing().when(productMapper).updateFromDTO(requestDto, product);
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toDto(product)).thenReturn(responseDto);

        ProductResponseDto result = productService.update(productId, requestDto);

        assertEquals(responseDto, result);
        verify(productMapper).updateFromDTO(requestDto, product);
        verify(productRepository).save(product);
    }

    @Test
    void update_nonexistentProduct_shouldThrow() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> productService.update(productId, requestDto));
    }

    @Test
    void getProducts_shouldReturnPageOfProducts() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(List.of(product));

        when(productRepository.findAll(pageable)).thenReturn(productPage);
        when(productMapper.toDto(product)).thenReturn(responseDto);

        Page<ProductResponseDto> result = productService.getProducts(null, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(responseDto, result.getContent().get(0));
    }

    @Test
    void getProducts_withNameFilter_shouldSearchByName() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(List.of(product));

        when(productRepository.findByNameContainingIgnoreCase("test", pageable)).thenReturn(productPage);
        when(productMapper.toDto(product)).thenReturn(responseDto);

        Page<ProductResponseDto> result = productService.getProducts("test", pageable);

        assertEquals(1, result.getTotalElements());
        verify(productRepository).findByNameContainingIgnoreCase("test", pageable);
    }

    @Test
    void delete_existingProduct_shouldMarkAsVoided() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);

        productService.delete(productId);

        assertTrue(product.isVoided());
        verify(productRepository).save(product);
    }

    @Test
    void delete_nonexistentProduct_shouldThrow() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> productService.delete(productId));
    }

    @Test
    void dispense_enoughStock_shouldReduceQuantity() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toDto(product)).thenReturn(responseDto);

        ProductResponseDto result = productService.dispense(productId, 5);

        assertEquals(5, product.getQuantity());
        assertEquals(responseDto, result);
    }

    @Test
    void dispense_notEnoughStock_shouldThrow() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        assertThrows(ProductException.class, () -> productService.dispense(productId, 20));
    }

    @Test
    void restock_shouldIncreaseQuantity() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toDto(product)).thenReturn(responseDto);

        ProductResponseDto result = productService.restock(productId, 5);

        assertEquals(15, product.getQuantity());
        assertEquals(responseDto, result);
    }
}
