package io.ganeshannt.asm.ops.exception;

import java.util.ArrayList;
import java.util.List;

/**
 * Exception for business rule validation failures
 * <p>
 * Difference from Bean Validation (@Valid):
 * - Bean Validation: Structural validation (not null, format, range)
 * - Business Validation: Domain-specific rules (stock availability, price rules)
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


    public static OrderValidationException emptyOrder() {
        return new OrderValidationException("Order must contain at least one item");
    }


    public static OrderValidationException invalidTotal() {
        return new OrderValidationException(
                "Order total amount does not match sum of item prices"
        );
    }
}
