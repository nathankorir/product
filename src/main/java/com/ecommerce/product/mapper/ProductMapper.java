package com.ecommerce.product.mapper;

import com.ecommerce.product.model.Product;
import com.ecommerce.product.dto.ProductRequestDto;
import com.ecommerce.product.dto.ProductResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    Product toEntity(ProductRequestDto dto);

    ProductResponseDto toDto(Product product);

    void updateFromDTO(ProductRequestDto dto, @MappingTarget Product entity);
}
