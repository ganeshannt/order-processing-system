package io.ganeshannt.asm.ops.service;

import io.ganeshannt.asm.ops.dto.OrderRequestDTO;
import io.ganeshannt.asm.ops.dto.OrderResponseDTO;
import io.ganeshannt.asm.ops.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Order Service Interface
 *
 * @author Ganeshannt
 * @version 1.2
 * @since 2025-10-23
 */
public interface IOrderService {

    /**
     * Create a new order
     */
    OrderResponseDTO createOrder(OrderRequestDTO request);

    /**
     * Retrieve order by ID
     */
    OrderResponseDTO getOrderById(Long id);

    /**
     * Get all orders with pagination support
     *
     * @param status Optional status filter
     * @param pageable Pagination parameters (page, size, sort)
     * @return Page of orders
     */
    Page<OrderResponseDTO> getAllOrders(OrderStatus status, Pageable pageable);

    /**
     * Cancel an order (only PENDING orders can be cancelled)
     */
    OrderResponseDTO cancelOrder(Long id);

    /**
     * Update order status
     */
    OrderResponseDTO updateOrderStatus(Long id, OrderStatus newStatus);

    /**
     * Promote PENDING orders to PROCESSING status
     *
     * @return Number of orders successfully promoted
     */
    int promotePendingOrdersToProcessing();
}
