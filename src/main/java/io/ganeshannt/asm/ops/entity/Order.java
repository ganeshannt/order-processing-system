package io.ganeshannt.asm.ops.entity;

import io.ganeshannt.asm.ops.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Order Entity - Core Domain Model
 * <p>
 * Design Decisions:
 * 1. BigDecimal for money: Precise decimal arithmetic (never use double/float for money)
 * 2. @CreationTimestamp/@UpdateTimestamp: Automatic timestamp management by Hibernate
 * 3. CascadeType.ALL: Child entities (OrderItems) lifecycle tied to parent (Order)
 * 4. orphanRemoval=true: Remove OrderItems when removed from collection
 * 5. FetchType.LAZY: Avoid N+1 query problem, load items only when needed
 * 6. @ToString.Exclude/@EqualsAndHashCode.Exclude: Prevent infinite recursion in bidirectional relationships
 * <p>
 * Why these Lombok annotations?
 * - @Data: Generates getters, setters, toString, equals, hashCode
 * - @Builder: Fluent API for object creation
 * - @NoArgsConstructor: Required by JPA
 * - @AllArgsConstructor: For builder pattern
 */
@Entity
@Table(
        name = "orders",
        indexes = {
                @Index(name = "idx_order_status", columnList = "status"),
                @Index(name = "idx_order_created_at", columnList = "created_at")
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Column(name = "customer_email", nullable = false, length = 255)
    private String customerEmail;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    /**
     * Bidirectional One-to-Many relationship
     * <p>
     * Why mappedBy?
     * - Indicates Order is NOT the owner of the relationship
     * - OrderItem.order field owns the foreign key
     * <p>
     * Why cascade ALL?
     * - When Order is saved, OrderItems are automatically saved
     * - When Order is deleted, OrderItems are automatically deleted
     * <p>
     * Why orphanRemoval?
     * - When OrderItem is removed from list, it's deleted from database
     */
    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<OrderItem> items = new ArrayList<>();

    /**
     * Helper method to add OrderItem
     * <p>
     * Why this method?
     * - Maintains bidirectional relationship consistency
     * - Ensures both sides of the relationship are set
     * - Prevents common bugs with JPA relationships
     * <p>
     * Best Practice: Always use helper methods for bidirectional relationships
     */
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    /**
     * Helper method to remove OrderItem
     */
    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
    }

    /**
     * JPA Lifecycle Callback
     * <p>
     * Why @PrePersist?
     * - Executes before entity is first persisted to database
     * - Sets default values
     * - Initializes state
     */
    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = OrderStatus.PENDING;
        }
        // Calculate total amount from items
        if (totalAmount == null && !items.isEmpty()) {
            totalAmount = items.stream()
                    .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
    }

    /**
     * Business method to check if order can be cancelled
     */
    public boolean canBeCancelled() {
        return status == OrderStatus.PENDING;
    }
}
