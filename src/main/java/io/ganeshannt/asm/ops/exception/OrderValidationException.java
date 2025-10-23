package io.ganeshannt.asm.ops.exception;

import java.util.ArrayList;
import java.util.List;

/**
 * Exception for business rule validation failures
 *
 * Difference from Bean Validation (@Valid):
 * - Bean Validation: Structural validation (not null, format, range)
 * - Business Validation: Domain-specific rules (stock availability, price rules)
 *
 * Why separate exception?
 * - Can contain multiple validation errors
 * - Business rules vs structural rules
 * - Different handling in global exception handler
 */
public class OrderValidationException extends RuntimeException {

    private final List<String> validationErrors;

    public OrderValidationException(String message) {
        super(message);
        this.validationErrors = new ArrayList<>();
        this.validationErrors.add(message);
    }

    public OrderValidationException(List<String> validationErrors) {
        super("Order validation failed: " + String.join(", ", validationErrors));
        this.validationErrors = new ArrayList<>(validationErrors);
    }

    public List<String> getValidationErrors() {
        return new ArrayList<>(validationErrors);
    }

    /**
     * Factory method for empty order
     */
    public static OrderValidationException emptyOrder() {
        return new OrderValidationException("Order must contain at least one item");
    }

    /**
     * Factory method for invalid total
     */
    public static OrderValidationException invalidTotal() {
        return new OrderValidationException(
                "Order total amount does not match sum of item prices"
        );
    }
}
