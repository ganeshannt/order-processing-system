package io.ganeshannt.asm.ops.exception;

/**
 * Exception thrown when an order is not found
 *
 * Why custom exceptions?
 * 1. Semantic clarity: OrderNotFoundException vs generic RuntimeException
 * 2. Specific handling: Can catch and handle differently
 * 3. Clean code: Self-documenting exception type
 * 4. HTTP status mapping: Maps to 404 NOT FOUND
 *
 * Why extend RuntimeException instead of Exception?
 * - RuntimeException: Unchecked, doesn't require try-catch everywhere
 * - Exception: Checked, forces try-catch (verbose, pollutes code)
 * - Spring handles RuntimeExceptions gracefully via @ControllerAdvice
 *
 * Design Pattern: Fail-fast
 * - Throw immediately when order not found
 * - Don't return null (NullPointerException waiting to happen)
 * - Let global handler format response
 */
public class OrderNotFoundException extends RuntimeException {

    /**
     * Constructor with message
     *
     * @param message Human-readable error message
     */
    public OrderNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructor with message and cause
     *
     * @param message Human-readable error message
     * @param cause Original exception that caused this
     */
    public OrderNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Convenience factory method
     *
     * Usage: throw OrderNotFoundException.withId(123L);
     * Result: "Order not found with ID: 123"
     */
    public static OrderNotFoundException withId(Long id) {
        return new OrderNotFoundException("Order not found with ID: " + id);
    }
}
