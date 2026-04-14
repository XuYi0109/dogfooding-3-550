package com.example.ecommerce.cart.repository;

import com.example.ecommerce.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<CartItem, Long> {
    
    List<CartItem> findByUserId(Long userId);
    
    Optional<CartItem> findByUserIdAndProductId(Long userId, Long productId);
    
    Optional<CartItem> findByIdAndUserId(Long id, Long userId);
    
    List<CartItem> findByUserIdAndSelectedTrue(Long userId);
    
    void deleteByUserId(Long userId);
    
    void deleteByIdAndUserId(Long id, Long userId);
    
    @Modifying
    @Query("DELETE FROM CartItem c WHERE c.userId = :userId AND c.id IN :ids")
    void deleteByIdsAndUserId(@Param("userId") Long userId, @Param("ids") List<Long> ids);
    
    @Modifying
    @Query("UPDATE CartItem c SET c.selected = :selected WHERE c.userId = :userId AND c.id = :id")
    void updateSelectedStatus(@Param("userId") Long userId, @Param("id") Long id, @Param("selected") boolean selected);
    
    long countByUserId(Long userId);
    
    boolean existsByUserIdAndProductId(Long userId, Long productId);
}
