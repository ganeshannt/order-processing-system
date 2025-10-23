package io.ganeshannt.asm.ops.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Individual item in an order")
public class OrderItemDTO {

    @Schema(description = "Item ID", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "Product name is required")
    @Size(min = 1, max = 200, message = "Product name must be between 1 and 200 characters")
    @Schema(
            description = "Name of the product",
            example = "Laptop",
            required = true
    )
    private String productName;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 1000, message = "Quantity cannot exceed 1000")
    @Schema(
            description = "Quantity of items",
            example = "2",
            minimum = "1",
            maximum = "1000",
            required = true
    )
    private Integer quantity;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be at least 0.01")
    @DecimalMax(value = "1000000.00", message = "Price cannot exceed 1,000,000.00")
    @Digits(integer = 10, fraction = 2, message = "Price must have at most 10 digits and 2 decimal places")
    @Schema(
            description = "Price per unit",
            example = "1299.99",
            required = true
    )
    private BigDecimal price;

    @Schema(
            description = "Total price for this line item (quantity × price)",
            example = "2599.98",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private BigDecimal lineTotal;
}
