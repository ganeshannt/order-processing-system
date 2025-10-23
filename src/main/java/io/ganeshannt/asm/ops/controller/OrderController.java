package io.ganeshannt.asm.ops.controller;

import io.ganeshannt.asm.ops.dto.OrderRequestDTO;
import io.ganeshannt.asm.ops.dto.OrderResponseDTO;
import io.ganeshannt.asm.ops.enums.OrderStatus;
import io.ganeshannt.asm.ops.service.IOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Ganeshannt
 * @version 1.0
 * @since 2025-10-23
 */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(
        name = "Order Management",
        description = "APIs for managing e-commerce orders including creation, retrieval, and status updates"
)
public class OrderController {

    private final IOrderService orderService;


    @PostMapping
    @Operation(
            summary = "Create a new order",
            description = "Creates a new order with the provided items. Order starts in PENDING status."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Order created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OrderResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request - validation failed",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<OrderResponseDTO> createOrder(
            @Valid @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Order details including customer email and items",
                    required = true
            )
            OrderRequestDTO request) {

        log.info("POST /api/v1/orders - Creating order for customer: {}",
                request.getCustomerEmail());

        OrderResponseDTO response = orderService.createOrder(request);

        log.info("Order created successfully - ID: {}", response.getId());

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get order by ID",
            description = "Retrieves detailed information about a specific order including all items"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Order found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OrderResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Order not found",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid order ID format",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<OrderResponseDTO> getOrderById(
            @Parameter(description = "Order ID", example = "1", required = true)
            @PathVariable Long id) {

        log.debug("GET /api/v1/orders/{} - Fetching order", id);

        OrderResponseDTO response = orderService.getOrderById(id);

        return ResponseEntity.ok(response);
    }

    /**
     * Get all orders with optional status filter
     *
     * HTTP Method: GET
     *
     * Query Parameters:
     * - status (optional): Filter by order status
     *
     * Examples:
     * - GET /api/v1/orders → All orders
     * - GET /api/v1/orders?status=PENDING → Only PENDING orders
     *
     * @RequestParam(required = false):
     * - Optional parameter
     * - null if not provided
     * - Automatic enum conversion
     *
     * Production Enhancement:
     * - Add pagination (Pageable)
     * - Add sorting options
     * - Add more filters (date range, customer, etc.)
     */
    @GetMapping
    @Operation(
            summary = "Get all orders",
            description = "Retrieves all orders, optionally filtered by status. " +
                    "In production, this should be paginated."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Orders retrieved successfully (may be empty list)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OrderResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid status value",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<List<OrderResponseDTO>> getAllOrders(
            @Parameter(
                    description = "Filter by order status (optional)",
                    example = "PENDING"
            )
            @RequestParam(required = false) OrderStatus status) {

        log.debug("GET /api/v1/orders - Fetching orders" +
                (status != null ? " with status: " + status : ""));

        List<OrderResponseDTO> response = orderService.getAllOrders(status);

        log.debug("Returning {} orders", response.size());

        return ResponseEntity.ok(response);
    }

    /**
     * Cancel order
     *
     * HTTP Method: PUT
     * Why PUT? Updating resource state (idempotent)
     *
     * Business Rule: Only PENDING orders can be cancelled
     *
     * Status Codes:
     * - 200 OK: Order cancelled successfully
     * - 404 NOT FOUND: Order doesn't exist
     * - 400 BAD REQUEST: Order cannot be cancelled (not PENDING)
     *
     * Idempotency:
     * - Cancelling already-cancelled order returns 400 (business rule)
     * - Multiple requests produce same outcome
     *
     * Alternative Design:
     * - Could use PATCH for partial update
     * - Could use DELETE (but order shouldn't be deleted, just cancelled)
     * - PUT is most semantically correct
     */
    @PutMapping("/{id}/cancel")
    @Operation(
            summary = "Cancel an order",
            description = "Cancels an order. Only PENDING orders can be cancelled."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Order cancelled successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OrderResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Order not found",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Order cannot be cancelled (not in PENDING status)",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<OrderResponseDTO> cancelOrder(
            @Parameter(description = "Order ID to cancel", example = "1", required = true)
            @PathVariable Long id) {

        log.info("PUT /api/v1/orders/{}/cancel - Cancelling order", id);

        OrderResponseDTO response = orderService.cancelOrder(id);

        log.info("Order {} cancelled successfully", id);

        return ResponseEntity.ok(response);
    }

    /**
     * Update order status
     *
     * HTTP Method: PUT
     *
     * Use Case: Admin/system updates order status
     * - PENDING → PROCESSING
     * - PROCESSING → SHIPPED
     * - SHIPPED → DELIVERED
     *
     * Security Note:
     * - In production, add authorization
     * - Only admin/system should access this endpoint
     * - Consider role-based access control
     *
     * Why separate from cancel?
     * - Different business logic
     * - Different authorization requirements
     * - Cancel is customer-facing, this is admin-facing
     * - Clearer API semantics
     */
    @PutMapping("/{id}/status")
    @Operation(
            summary = "Update order status",
            description = "Updates order status. Validates state transitions. " +
                    "Should be restricted to admin users in production."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Order status updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OrderResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Order not found",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid status transition",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<OrderResponseDTO> updateOrderStatus(
            @Parameter(description = "Order ID", example = "1", required = true)
            @PathVariable Long id,
            @Parameter(description = "New order status", example = "PROCESSING", required = true)
            @RequestParam OrderStatus status) {

        log.info("PUT /api/v1/orders/{}/status - Updating to {}", id, status);

        OrderResponseDTO response = orderService.updateOrderStatus(id, status);

        log.info("Order {} status updated to {}", id, status);

        return ResponseEntity.ok(response);
    }
}
