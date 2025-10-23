package io.ganeshannt.asm.ops.exception;

import io.ganeshannt.asm.ops.enums.OrderStatus;

/**
 * Exception thrown when attempting invalid order status transition
 * <p>
 * Use Cases:
 * - Trying to cancel non-PENDING order
 * - Invalid status transition (e.g., DELIVERED → PENDING)
 * - Business rule violation
 * <p>
 * Maps to HTTP 400 BAD REQUEST
 * - Client sent invalid request (not server error)
 * - Indicates a client should not retry without modification
 */
public class InvalidOrderStatusException extends RuntimeException {

    public InvalidOrderStatusException(String message) {
        super(message);
    }

    public static InvalidOrderStatusException cannotCancel(OrderStatus currentStatus) {
        return new InvalidOrderStatusException(
                String.format("Cannot cancel order with status %s. Only PENDING orders can be cancelled.",
                        currentStatus)
        );
    }


    public static InvalidOrderStatusException invalidTransition(
            OrderStatus from, OrderStatus to) {
        return new InvalidOrderStatusException(
                String.format("Invalid status transition from %s to %s", from, to)
        );
    }
}
