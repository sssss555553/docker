package com.ecommerce.service;

import com.ecommerce.entity.Product;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

/**
 * 商品业务逻辑层
 * 提供商品CRUD及查询服务
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ProductService {
    
    private final ProductRepository productRepository;
    
    /**
     * 获取所有商品
     */
    @Transactional(readOnly = true)
    public List<Product> findAll() {
        log.info("查询所有商品");
        return productRepository.findAll();
    }
    
    /**
     * 根据ID获取商品
     */
    @Transactional(readOnly = true)
    public Product findById(Long id) {
        log.info("查询商品: id={}", id);
        return productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("商品不存在: " + id));
    }
    
    /**
     * 创建商品
     */
    public Product create(Product product) {
        log.info("创建商品: {}", product.getName());
        return productRepository.save(product);
    }
    
    /**
     * 更新商品
     */
    public Product update(Long id, Product product) {
        log.info("更新商品: id={}", id);
        Product existing = findById(id);
        existing.setName(product.getName());
        existing.setDescription(product.getDescription());
        existing.setPrice(product.getPrice());
        existing.setStock(product.getStock());
        existing.setImageUrl(product.getImageUrl());
        existing.setCategory(product.getCategory());
        return productRepository.save(existing);
    }
    
    /**
     * 删除商品
     */
    public void delete(Long id) {
        log.info("删除商品: id={}", id);
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("商品不存在: " + id);
        }
        productRepository.deleteById(id);
    }
    
    /**
     * 按分类查询
     */
    @Transactional(readOnly = true)
    public List<Product> findByCategory(String category) {
        log.info("按分类查询商品: category={}", category);
        return productRepository.findByCategory(category);
    }
    
    /**
     * 按名称搜索
     */
    @Transactional(readOnly = true)
    public List<Product> searchByName(String name) {
        log.info("搜索商品: name={}", name);
        return productRepository.findByNameContaining(name);
    }
    
    /**
     * 按价格范围查询
     */
    @Transactional(readOnly = true)
    public List<Product> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        log.info("按价格范围查询: {} - {}", minPrice, maxPrice);
        return productRepository.findByPriceBetween(minPrice, maxPrice);
    }
}
