package io.ganeshannt.asm.ops.controller;

import io.ganeshannt.asm.ops.dto.OrderRequestDTO;
import io.ganeshannt.asm.ops.dto.OrderResponseDTO;
import io.ganeshannt.asm.ops.dto.PagedResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Order REST Controller with Pagination Support
 *
 * Pagination Design:
 * - Page numbers: 1-1000 (user-friendly, 1-indexed)
 * - Default page: 1
 * - Default size: 10
 * - Fixed sorting: createdAt DESC (newest first)
 *
 * @author Ganeshannt
 * @version 1.3
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

    /**
     * Pagination configuration constants
     */
    private static final int DEFAULT_PAGE = 1;           // User-friendly: starts from 1
    private static final int DEFAULT_SIZE = 10;          // Balanced page size
    private static final int MIN_PAGE = 1;               // Minimum page number
    private static final int MAX_PAGE = 1000;            // Maximum page number
    private static final int MIN_SIZE = 1;               // Minimum page size
    private static final int MAX_SIZE = 100;             // Maximum page size

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
     * Get all orders with pagination support
     *
     * Pagination Design:
     * ==================
     *
     * Page Numbering: 1-indexed (user-friendly)
     * - page=1 → First page (NOT page=0)
     * - page=2 → Second page
     * - Valid range: 1-1000
     *
     * Sorting: Fixed (createdAt DESC)
     * - Orders always sorted by creation date
     * - Newest orders appear first
     * - No custom sorting parameters needed
     *
     * Parameters:
     * ===========
     *
     * @param page   Page number (1-1000), default: 1
     * @param size   Page size (1-100), default: 10
     * @param status Optional status filter
     *
     * Examples:
     * =========
     *
     * 1. Default: First page, 10 items
     *    GET /api/v1/orders
     *
     * 2. Get page 2 with 20 items:
     *    GET /api/v1/orders?page=2&size=20
     *
     * 3. Filter PENDING orders:
     *    GET /api/v1/orders?status=PENDING
     *
     * 4. Filter SHIPPED orders, page 2:
     *    GET /api/v1/orders?status=SHIPPED&page=2
     *
     * 5. Get 50 items per page:
     *    GET /api/v1/orders?size=50
     *
     * Response Format:
     * ===============
     * {
     *   "content": [...],        // Array of orders (sorted by createdAt DESC)
     *   "pageNumber": 1,         // Current page (1-indexed)
     *   "pageSize": 10,          // Items per page
     *   "totalElements": 100,    // Total items
     *   "totalPages": 10,        // Total pages
     *   "first": true,           // Is first page?
     *   "last": false,           // Is last page?
     *   "empty": false           // Is empty?
     * }
     *
     * Validation:
     * ===========
     * - page: Clamped to 1-1000
     * - size: Clamped to 1-100
     * - Invalid values automatically corrected
     *
     * @return Paginated response with orders sorted by createdAt DESC
     */
    @GetMapping
    @Operation(
            summary = "Get all orders with pagination",
            description = """
            Retrieves orders with pagination support.
            
            **Page Numbering**: 1-indexed (starts from 1, not 0)
            
            **Sorting**: Fixed - Orders sorted by creation date (newest first)
            
            **Default Behavior**: Returns page 1 with 10 orders
            
            **Pagination Parameters**:
            - `page`: Page number (1-1000), default: 1
            - `size`: Items per page (1-100), default: 10
            - `status`: Optional filter by order status
            
            **Examples**:
            ```
            GET /api/v1/orders                        → Page 1, 10 items
            GET /api/v1/orders?page=2&size=20         → Page 2, 20 items
            GET /api/v1/orders?status=PENDING         → PENDING orders, page 1
            GET /api/v1/orders?status=SHIPPED&page=3  → SHIPPED orders, page 3
            ```
            
            **Note**: Orders are always sorted by creation date (newest first).
            """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Orders retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PagedResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid pagination parameters",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<PagedResponse<OrderResponseDTO>> getAllOrders(
            @Parameter(
                    description = "Page number (1-1000, 1-indexed)",
                    example = "1"
            )
            @RequestParam(defaultValue = "1") int page,

            @Parameter(
                    description = "Number of items per page (1-100)",
                    example = "10"
            )
            @RequestParam(defaultValue = "10") int size,

            @Parameter(
                    description = "Filter by order status (optional)",
                    example = "PENDING"
            )
            @RequestParam(required = false) OrderStatus status) {

        // Validate and sanitize pagination parameters
        // Clamp page to valid range: 1-1000
        page = Math.max(MIN_PAGE, Math.min(page, MAX_PAGE));

        // Clamp size to valid range: 1-100
        size = Math.max(MIN_SIZE, Math.min(size, MAX_SIZE));

        log.debug("GET /api/v1/orders - Page: {}, Size: {}, Status: {}",
                page, size, status);

        // Convert 1-indexed page to 0-indexed for Spring Data
        int zeroIndexedPage = page - 1;

        // Create Pageable with fixed sorting: createdAt DESC (newest first)
        Pageable pageable = PageRequest.of(
                zeroIndexedPage,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        // Get paginated results from service
        Page<OrderResponseDTO> ordersPage = orderService.getAllOrders(status, pageable);

        // Convert to custom PagedResponse DTO (converts back to 1-indexed)
        PagedResponse<OrderResponseDTO> response = PagedResponse.of(ordersPage);

        log.debug("Returning page {} of {} ({} items)",
                response.getPageNumber(),
                response.getTotalPages(),
                response.getContent().size());

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/cancel")
    @Operation(
            summary = "Cancel an order",
            description = """
            Cancels an order by updating its status to CANCELLED.
            
            **HTTP Method**: PATCH (partial update)
            
            **Business Rule**: Only PENDING orders can be cancelled.
            """
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
                    responseCode = "400",
                    description = "Order cannot be cancelled (not in PENDING status)",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Order not found",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<OrderResponseDTO> cancelOrder(
            @Parameter(description = "Order ID to cancel", example = "1", required = true)
            @PathVariable Long id) {

        log.info("PATCH /api/v1/orders/{}/cancel - Cancelling order", id);

        OrderResponseDTO response = orderService.cancelOrder(id);

        log.info("Order {} cancelled successfully", id);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/status")
    @Operation(
            summary = "Update order status",
            description = """
            Updates order status. Validates state transitions.
            
            **Valid Transitions**:
            - PENDING → PROCESSING ✓
            - PENDING → CANCELLED ✓
            - PROCESSING → SHIPPED ✓
            - SHIPPED → DELIVERED ✓
            """
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
