package com.example.ecommerce.cart.repository;

import com.example.ecommerce.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    List<Cart> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Cart> findByUserIdAndProductId(Long userId, Long productId);

    boolean existsByUserIdAndProductId(Long userId, Long productId);

    @Modifying
    @Query("DELETE FROM Cart c WHERE c.user.id = :userId AND c.id IN :ids")
    void deleteAllByUserIdAndIdIn(@Param("userId") Long userId, @Param("ids") List<Long> ids);

    @Modifying
    @Query("DELETE FROM Cart c WHERE c.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);

    Integer countByUserId(Long userId);
}
