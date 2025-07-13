package com.ecommerce.product.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ecommerce.product.dto.ProductRequestDto;
import com.ecommerce.product.dto.ProductResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ProductControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void whenCreateProductThenSuccess() throws Exception {
        ProductRequestDto request = new ProductRequestDto("Samsung Galaxy", 10, BigDecimal.valueOf(10000));

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Samsung Galaxy"))
                .andExpect(jsonPath("$.quantity").value(10))
                .andExpect(jsonPath("$.price").value(BigDecimal.valueOf(10000)));
    }


    @Test
    void whenGetProductByIdThenReturnProduct() throws Exception {
        ProductRequestDto request = new ProductRequestDto("Samsung Galaxy", 10, BigDecimal.valueOf(10000));

        String response = mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ProductResponseDto created = objectMapper.readValue(response, ProductResponseDto.class);
        UUID productId = created.getId();

        mockMvc.perform(get("/products/" + "/" + productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId.toString()))
                .andExpect(jsonPath("$.name").value("Samsung Galaxy"));
    }

    @Test
    void whenUpdateProductThenSuccess() throws Exception {
        ProductRequestDto request = new ProductRequestDto("Samsung Galaxy", 10, BigDecimal.valueOf(10000));

        String response = mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ProductResponseDto created = objectMapper.readValue(response, ProductResponseDto.class);
        UUID productId = created.getId();

        ProductRequestDto updateRequest = new ProductRequestDto("Samsung Galaxy S3", 15, BigDecimal.valueOf(11000));

        mockMvc.perform(put("/products" + "/" + productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(updateRequest.getName()))
                .andExpect(jsonPath("$.quantity").value(updateRequest.getQuantity()))
                .andExpect(jsonPath("$.price").value(updateRequest.getPrice()));
    }

    @Test
    void whenListProductsThenSuccess() throws Exception {
        ProductRequestDto request = new ProductRequestDto("Samsung Galaxy S4", 10, BigDecimal.valueOf(12000));

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        mockMvc.perform(get("/products")
                        .param("name", "Samsung Galaxy S4")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists());
    }

    @Test
    void whenSoftDeleteProductThenSuccess() throws Exception {
        ProductRequestDto request = new ProductRequestDto("Samsung Galaxy S5", 8, BigDecimal.valueOf(13000));

        String response = mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ProductResponseDto created = objectMapper.readValue(response, ProductResponseDto.class);
        UUID productId = created.getId();

        mockMvc.perform(delete("/products" + "/" + productId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/products" + "/" + productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId.toString()))
                .andExpect(jsonPath("$.voided").value("false"));
    }
}
