package com.example.ecommerce.product.repository;

import com.example.ecommerce.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    Page<Product> findByStatus(Product.ProductStatus status, Pageable pageable);
    
    Page<Product> findByCategory(Product.ProductCategory category, Pageable pageable);
    
    Page<Product> findByCategoryAndStatus(Product.ProductCategory category, 
                                         Product.ProductStatus status, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE " +
           "(:name IS NULL OR p.name LIKE %:name%) AND " +
           "(:category IS NULL OR p.category = :category) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
           "p.status = 'ACTIVE'")
    Page<Product> searchProducts(@Param("name") String name,
                                @Param("category") Product.ProductCategory category,
                                @Param("minPrice") BigDecimal minPrice,
                                @Param("maxPrice") BigDecimal maxPrice,
                                Pageable pageable);
    
    List<Product> findByStockQuantityLessThanEqual(int stockQuantity);
    
    boolean existsByName(String name);
}