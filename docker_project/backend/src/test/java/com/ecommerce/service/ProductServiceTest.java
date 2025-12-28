package com.ecommerce.service;

import com.ecommerce.entity.Product;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 商品服务单元测试
 * 测试覆盖率目标 >= 80%
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
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
    @DisplayName("查询所有商品")
    void findAll_ShouldReturnAllProducts() {
        List<Product> products = Arrays.asList(testProduct);
        when(productRepository.findAll()).thenReturn(products);

        List<Product> result = productService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("测试商品");
        verify(productRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("根据ID查询商品 - 存在")
    void findById_WhenExists_ShouldReturnProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        Product result = productService.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("测试商品");
    }

    @Test
    @DisplayName("根据ID查询商品 - 不存在")
    void findById_WhenNotExists_ShouldThrowException() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("商品不存在");
    }

    @Test
    @DisplayName("创建商品")
    void create_ShouldSaveAndReturnProduct() {
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        Product result = productService.create(testProduct);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("更新商品")
    void update_WhenExists_ShouldUpdateAndReturn() {
        Product updateData = Product.builder()
                .name("更新后的商品")
                .price(new BigDecimal("199.99"))
                .stock(50)
                .build();
        
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        Product result = productService.update(1L, updateData);

        assertThat(result).isNotNull();
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("删除商品 - 存在")
    void delete_WhenExists_ShouldDelete() {
        when(productRepository.existsById(1L)).thenReturn(true);
        doNothing().when(productRepository).deleteById(1L);

        productService.delete(1L);

        verify(productRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("删除商品 - 不存在")
    void delete_WhenNotExists_ShouldThrowException() {
        when(productRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> productService.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("按分类查询")
    void findByCategory_ShouldReturnProducts() {
        List<Product> products = Arrays.asList(testProduct);
        when(productRepository.findByCategory("测试分类")).thenReturn(products);

        List<Product> result = productService.findByCategory("测试分类");

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("按名称搜索")
    void searchByName_ShouldReturnMatchingProducts() {
        List<Product> products = Arrays.asList(testProduct);
        when(productRepository.findByNameContaining("测试")).thenReturn(products);

        List<Product> result = productService.searchByName("测试");

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("按价格范围查询")
    void findByPriceRange_ShouldReturnProducts() {
        List<Product> products = Arrays.asList(testProduct);
        BigDecimal min = new BigDecimal("50");
        BigDecimal max = new BigDecimal("150");
        when(productRepository.findByPriceBetween(min, max)).thenReturn(products);

        List<Product> result = productService.findByPriceRange(min, max);

        assertThat(result).hasSize(1);
    }
}
