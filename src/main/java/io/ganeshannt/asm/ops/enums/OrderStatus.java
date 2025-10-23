package io.ganeshannt.asm.ops.enums;

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


    public boolean canTransitionTo(OrderStatus newStatus) {
        return switch (this) {
            case PENDING -> newStatus == PROCESSING || newStatus == CANCELLED;
            case PROCESSING -> newStatus == SHIPPED || newStatus == CANCELLED;
            case SHIPPED -> newStatus == DELIVERED;
            case DELIVERED, CANCELLED -> false; // Terminal states
        };
    }
}
