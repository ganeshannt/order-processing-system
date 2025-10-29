package io.ganeshannt.asm.ops.config;

import io.ganeshannt.asm.ops.entity.Order;
import io.ganeshannt.asm.ops.entity.OrderItem;
import io.ganeshannt.asm.ops.enums.OrderStatus;
import io.ganeshannt.asm.ops.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 *
 * @author Ganeshannt
 * @version 1.0
 * @since 2025-10-23
 */
@Configuration
@Profile("test")
@RequiredArgsConstructor
@Slf4j
public class TestDataInitializer {

    @Bean
    public CommandLineRunner initializeTestData(OrderRepository orderRepository) {
        return args -> {
            log.info("🧪 Initializing test data...");

            // Order 1: PENDING
            Order order1 = createOrder("pending@test.com", OrderStatus.PENDING);
            order1.addItem(createItem("Test Product 1", 2, "50.00"));
            orderRepository.save(order1);

            // Order 2: PROCESSING
            Order order2 = createOrder("processing@test.com", OrderStatus.PROCESSING);
            order2.addItem(createItem("Test Product 2", 1, "100.00"));
            orderRepository.save(order2);

            // Order 3: SHIPPED
            Order order3 = createOrder("shipped@test.com", OrderStatus.SHIPPED);
            order3.addItem(createItem("Test Product 3", 3, "25.00"));
            orderRepository.save(order3);

            // Order 4: DELIVERED
            Order order4 = createOrder("delivered@test.com", OrderStatus.DELIVERED);
            order4.addItem(createItem("Test Product 4", 1, "75.00"));
            orderRepository.save(order4);

            // Order 5: CANCELLED
            Order order5 = createOrder("cancelled@test.com", OrderStatus.CANCELLED);
            order5.addItem(createItem("Test Product 5", 2, "30.00"));
            orderRepository.save(order5);

            long totalOrders = orderRepository.count();
            log.info("Test data initialized - Total Orders: {}", totalOrders);
        };
    }

    private Order createOrder(String email, OrderStatus status) {
        return Order.builder()
                .customerEmail(email)
                .status(status)
                .totalAmount(BigDecimal.ZERO)
                .build();
    }

    private OrderItem createItem(String productName, int quantity, String price) {
        return OrderItem.builder()
                .productName(productName)
                .quantity(quantity)
                .price(new BigDecimal(price))
                .build();
    }
}
