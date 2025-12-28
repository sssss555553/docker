package com.ecommerce.repository;

import com.ecommerce.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

/**
 * 商品数据访问层
 * 继承JpaRepository获得基础CRUD功能
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    /**
     * 按分类查询商品
     */
    List<Product> findByCategory(String category);
    
    /**
     * 按名称模糊查询
     */
    List<Product> findByNameContaining(String name);
    
    /**
     * 按价格范围查询
     */
    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
    
    /**
     * 查询有库存的商品
     */
    List<Product> findByStockGreaterThan(Integer stock);
    
    /**
     * 按分类和价格范围查询
     */
    @Query("SELECT p FROM Product p WHERE p.category = :category AND p.price BETWEEN :minPrice AND :maxPrice")
    List<Product> findByCategoryAndPriceRange(
            @Param("category") String category,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice);
}
