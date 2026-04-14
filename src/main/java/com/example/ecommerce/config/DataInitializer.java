package com.example.ecommerce.config;

import com.example.ecommerce.product.entity.Product;
import com.example.ecommerce.product.repository.ProductRepository;
import com.example.ecommerce.user.entity.User;
import com.example.ecommerce.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("Initializing test data...");

        if (userRepository.count() == 0) {
            User admin = User.builder()
                    .username("admin")
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("admin123"))
                    .firstName("Admin")
                    .lastName("User")
                    .role(User.UserRole.ROLE_ADMIN)
                    .enabled(true)
                    .build();

            User user = User.builder()
                    .username("testuser")
                    .email("user@example.com")
                    .password(passwordEncoder.encode("user123"))
                    .firstName("Test")
                    .lastName("User")
                    .role(User.UserRole.ROLE_USER)
                    .enabled(true)
                    .build();

            userRepository.saveAll(Arrays.asList(admin, user));
            log.info("Test users created: admin/admin123, testuser/user123");
        }

        if (productRepository.count() == 0) {
            Product product1 = Product.builder()
                    .name("iPhone 15 Pro")
                    .description("最新款苹果智能手机，A17 Pro芯片，钛金属设计")
                    .price(new BigDecimal("7999.00"))
                    .stockQuantity(50)
                    .imageUrl("https://example.com/iphone15.jpg")
                    .category(Product.ProductCategory.ELECTRONICS)
                    .status(Product.ProductStatus.ACTIVE)
                    .build();

            Product product2 = Product.builder()
                    .name("MacBook Pro 14")
                    .description("M3 Pro芯片，18GB内存，512GB存储")
                    .price(new BigDecimal("14999.00"))
                    .stockQuantity(30)
                    .imageUrl("https://example.com/macbook.jpg")
                    .category(Product.ProductCategory.ELECTRONICS)
                    .status(Product.ProductStatus.ACTIVE)
                    .build();

            Product product3 = Product.builder()
                    .name("运动跑步鞋")
                    .description("专业跑步鞋，透气舒适，减震缓冲")
                    .price(new BigDecimal("599.00"))
                    .stockQuantity(100)
                    .imageUrl("https://example.com/shoes.jpg")
                    .category(Product.ProductCategory.SPORTS)
                    .status(Product.ProductStatus.ACTIVE)
                    .build();

            Product product4 = Product.builder()
                    .name("Java编程思想")
                    .description("Bruce Eckel经典著作，Java开发必读")
                    .price(new BigDecimal("89.00"))
                    .stockQuantity(200)
                    .imageUrl("https://example.com/java-book.jpg")
                    .category(Product.ProductCategory.BOOKS)
                    .status(Product.ProductStatus.ACTIVE)
                    .build();

            productRepository.saveAll(Arrays.asList(product1, product2, product3, product4));
            log.info("Test products created: 4 products");
        }

        log.info("Data initialization completed!");
    }
}