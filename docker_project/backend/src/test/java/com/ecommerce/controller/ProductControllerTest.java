package com.ecommerce.controller;

import com.ecommerce.entity.Product;
import com.ecommerce.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 商品控制器集成测试
 */
@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
                .id(1L)
                .name("测试商品")
                .description("测试描述")
                .price(new BigDecimal("99.99"))
                .stock(100)
                .category("测试分类")
                .build();
    }

    @Test
    @DisplayName("GET /api/products - 获取所有商品")
    void getAllProducts_ShouldReturnList() throws Exception {
        List<Product> products = Arrays.asList(testProduct);
        when(productService.findAll()).thenReturn(products);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].name").value("测试商品"));
    }

    @Test
    @DisplayName("GET /api/products/{id} - 获取单个商品")
    void getProductById_ShouldReturnProduct() throws Exception {
        when(productService.findById(1L)).thenReturn(testProduct);

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("测试商品"))
                .andExpect(jsonPath("$.price").value(99.99));
    }

    @Test
    @DisplayName("POST /api/products - 创建商品")
    void createProduct_ShouldReturnCreated() throws Exception {
        when(productService.create(any(Product.class))).thenReturn(testProduct);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testProduct)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("POST /api/products - 创建商品参数验证失败")
    void createProduct_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        Product invalidProduct = Product.builder()
                .name("")  // 空名称
                .price(new BigDecimal("-1"))  // 负价格
                .build();

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidProduct)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/products/{id} - 更新商品")
    void updateProduct_ShouldReturnUpdated() throws Exception {
        when(productService.update(eq(1L), any(Product.class))).thenReturn(testProduct);

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testProduct)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/products/{id} - 删除商品")
    void deleteProduct_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/products/category/{category} - 按分类查询")
    void getByCategory_ShouldReturnProducts() throws Exception {
        List<Product> products = Arrays.asList(testProduct);
        when(productService.findByCategory("测试分类")).thenReturn(products);

        mockMvc.perform(get("/api/products/category/测试分类"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category").value("测试分类"));
    }

    @Test
    @DisplayName("GET /api/products/search - 按名称搜索")
    void searchByName_ShouldReturnProducts() throws Exception {
        List<Product> products = Arrays.asList(testProduct);
        when(productService.searchByName("测试")).thenReturn(products);

        mockMvc.perform(get("/api/products/search")
                        .param("name", "测试"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("测试商品"));
    }
}
