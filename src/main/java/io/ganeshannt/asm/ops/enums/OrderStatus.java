package io.ganeshannt.asm.ops.enums;

/**
 * Order Status Enum
 *
 * Design Decision: Using enum instead of String for type safety
 * - Compile-time validation
 * - Auto-completion in IDEs
 * - Cannot have invalid values
 *
 * State Transitions:
 * PENDING → PROCESSING (automatic via scheduler or manual)
 * PROCESSING → SHIPPED (manual)
 * SHIPPED → DELIVERED (manual)
 * PENDING → CANCELLED (only from PENDING state)
 */
public enum OrderStatus {
    /**
     * Initial state when order is created
     */
    PENDING,

    /**
     * Order is being processed (payment confirmed, items being prepared)
     */
    PROCESSING,

    /**
     * Order has been shipped to customer
     */
    SHIPPED,

    /**
     * Order successfully delivered to customer
     */
    DELIVERED,

    /**
     * Order was cancelled (only possible from PENDING state)
     */
    CANCELLED;

    /**
     * Check if status transition is valid
     *
     * Why this method?
     * - Centralizes business rule validation
     * - Prevents invalid state transitions
     * - Makes testing easier
     */
    public boolean canTransitionTo(OrderStatus newStatus) {
        return switch (this) {
            case PENDING -> newStatus == PROCESSING || newStatus == CANCELLED;
            case PROCESSING -> newStatus == SHIPPED || newStatus == CANCELLED;
            case SHIPPED -> newStatus == DELIVERED;
            case DELIVERED, CANCELLED -> false; // Terminal states
        };
    }
}
