package io.ganeshannt.asm.ops.dto;

import io.ganeshannt.asm.ops.enums.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Order response with complete details")
public class OrderResponseDTO {

    @Schema(description = "Unique order identifier", example = "1")
    private Long id;

    @Schema(description = "Current order status", example = "PENDING")
    private OrderStatus status;

    @Schema(description = "Customer email address", example = "customer@example.com")
    private String customerEmail;

    @Schema(description = "Total order amount", example = "2599.98")
    private BigDecimal totalAmount;

    @Schema(description = "Order creation timestamp", example = "2025-10-22T22:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp", example = "2025-10-22T22:30:00")
    private LocalDateTime updatedAt;

    @Schema(description = "List of items in this order")
    private List<OrderItemDTO> items;
}
