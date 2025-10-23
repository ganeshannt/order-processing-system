package io.ganeshannt.asm.ops.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for creating a new order")
public class OrderRequestDTO {


    @NotBlank(message = "Customer email is required")
    @Email(message = "Customer email must be valid")
    @Size(max = 255, message = "Customer email must not exceed 255 characters")
    @Schema(
            description = "Customer email address",
            example = "customer@example.com",
            required = true
    )
    private String customerEmail;


    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    @Schema(description = "List of items to order", required = true)
    private List<OrderItemDTO> items;
}
