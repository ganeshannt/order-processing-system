package io.ganeshannt.asm.ops.service;

import io.ganeshannt.asm.ops.dto.OrderRequestDTO;
import io.ganeshannt.asm.ops.dto.OrderResponseDTO;
import io.ganeshannt.asm.ops.enums.OrderStatus;

import java.util.List;

/**
 * @author Ganeshannt
 * @version 1.0
 * @since 2025-10-23
 */
public interface IOrderService {

    /**
     * Create a new order
     *
     * @param request Order creation request containing customer email and items
     * @return Created order with generated ID and initial status
     * @throws io.ganeshannt.asm.ops.exception.OrderValidationException if business rules violated
     */
    OrderResponseDTO createOrder(OrderRequestDTO request);

    /**
     * Retrieve order by ID
     *
     * @param id Order identifier
     * @return Order details including all items
     * @throws io.ganeshannt.asm.ops.exception.OrderNotFoundException if order doesn't exist
     */
    OrderResponseDTO getOrderById(Long id);

    /**
     * Retrieve all orders, optionally filtered by status
     *
     * @param status Optional status filter (null for all orders)
     * @return List of orders matching criteria
     */
    List<OrderResponseDTO> getAllOrders(OrderStatus status);

    /**
     * Cancel an order (only PENDING orders can be cancelled)
     *
     * @param id Order identifier
     * @return Updated order with CANCELLED status
     * @throws io.ganeshannt.asm.ops.exception.OrderNotFoundException if order doesn't exist
     * @throws io.ganeshannt.asm.ops.exception.InvalidOrderStatusException if order cannot be cancelled
     */
    OrderResponseDTO cancelOrder(Long id);

    /**
     * Update order status
     *
     * @param id Order identifier
     * @param newStatus New order status
     * @return Updated order
     * @throws io.ganeshannt.asm.ops.exception.OrderNotFoundException if order doesn't exist
     * @throws io.ganeshannt.asm.ops.exception.InvalidOrderStatusException if transition invalid
     */
    OrderResponseDTO updateOrderStatus(Long id, OrderStatus newStatus);

    /**
     * Promote PENDING orders to PROCESSING status
     *
     * This method is specifically designed for scheduler use.
     * It processes orders in batch and handles errors gracefully.
     *
     * Business Logic:
     * - Finds all PENDING orders
     * - Updates them to PROCESSING
     * - Returns count of successfully updated orders
     * - Logs failures but continues processing
     *
     * @return Number of orders successfully promoted
     */
    int promotePendingOrdersToProcessing();
}
