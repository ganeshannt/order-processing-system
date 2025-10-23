package io.ganeshannt.asm.ops.service;

import io.ganeshannt.asm.ops.dto.OrderRequestDTO;
import io.ganeshannt.asm.ops.dto.OrderResponseDTO;
import io.ganeshannt.asm.ops.entity.Order;
import io.ganeshannt.asm.ops.enums.OrderStatus;
import io.ganeshannt.asm.ops.exception.InvalidOrderStatusException;
import io.ganeshannt.asm.ops.exception.OrderNotFoundException;
import io.ganeshannt.asm.ops.exception.OrderValidationException;
import io.ganeshannt.asm.ops.mapper.OrderMapper;
import io.ganeshannt.asm.ops.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Order Service Implementation
 * @author Ganeshannt
 * @version 1.0
 * @since 2025-10-23
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrderServiceImpl implements IOrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    /**
     * {@inheritDoc}
     * <p>
     * Implementation Notes:
     * - Validates business rules before persistence
     * - Uses MapStruct for DTO-Entity conversion
     * - Calculates total amount from line items
     * - Logs for audit trail and monitoring
     */
    @Override
    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO request) {
        log.info("Creating order for customer: {} with {} items",
                request.getCustomerEmail(),
                request.getItems().size());

        // Business validation
        validateOrderRequest(request);

        // Convert DTO to Entity
        Order order = orderMapper.toEntity(request);

        // Business rule: Validate total amount
        if (order.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw OrderValidationException.invalidTotal();
        }

        // Persist
        Order savedOrder = orderRepository.save(order);

        log.info("Order created successfully - ID: {}, Total: {}, Status: {}",
                savedOrder.getId(),
                savedOrder.getTotalAmount(),
                savedOrder.getStatus());

        return orderMapper.toResponseDTO(savedOrder);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation Notes:
     * - Uses JOIN FETCH query to avoid N+1 problem
     * - Throws specific exception for 404 handling
     */
    @Override
    public OrderResponseDTO getOrderById(Long id) {
        log.debug("Fetching order with ID: {}", id);

        Order order = orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> OrderNotFoundException.withId(id));

        log.debug("Order found - ID: {}, Status: {}", order.getId(), order.getStatus());

        return orderMapper.toResponseDTO(order);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation Notes:
     * - Supports optional filtering by status
     * - Uses optimized query with JOIN FETCH when filtering
     * - Returns empty list if no orders found (not exception)
     */
    @Override
    public List<OrderResponseDTO> getAllOrders(OrderStatus status) {
        log.debug("Fetching all orders" + (status != null ? " with status: " + status : ""));

        List<Order> orders;
        if (status != null) {
            orders = orderRepository.findByStatusWithItems(status);
        } else {
            orders = orderRepository.findAll();
        }

        log.debug("Found {} orders", orders.size());
        return orderMapper.toResponseDTOList(orders);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation Notes:
     * - Business rule: Only PENDING orders can be cancelled
     * - Uses entity method canBeCancelled() for domain logic
     * - Throws specific exception for business rule violation
     */
    @Override
    @Transactional
    public OrderResponseDTO cancelOrder(Long id) {
        log.info("Attempting to cancel order with ID: {}", id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> OrderNotFoundException.withId(id));

        // Business rule validation
        if (!order.canBeCancelled()) {
            log.warn("Cannot cancel order {} - Current status: {}", id, order.getStatus());
            throw InvalidOrderStatusException.cannotCancel(order.getStatus());
        }

        // Update status
        order.setStatus(OrderStatus.CANCELLED);
        Order updatedOrder = orderRepository.save(order);

        log.info("Order {} cancelled successfully", id);
        return orderMapper.toResponseDTO(updatedOrder);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation Notes:
     * - Validates state transitions using enum method
     * - Prevents invalid status changes
     * - Useful for admin operations and workflow management
     */
    @Override
    @Transactional
    public OrderResponseDTO updateOrderStatus(Long id, OrderStatus newStatus) {
        log.info("Updating order {} to status: {}", id, newStatus);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> OrderNotFoundException.withId(id));

        // Validate status transition
        if (!order.getStatus().canTransitionTo(newStatus)) {
            log.warn("Invalid status transition for order {} from {} to {}",
                    id, order.getStatus(), newStatus);
            throw InvalidOrderStatusException.invalidTransition(
                    order.getStatus(), newStatus);
        }

        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);

        log.info("Order {} status updated from {} to {}",
                id, order.getStatus(), newStatus);

        return orderMapper.toResponseDTO(updatedOrder);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation Notes:
     * - Designed specifically for scheduler consumption
     * - Handles errors gracefully (continues on individual failures)
     * - Returns success count for monitoring
     * - Logs detailed metrics for observability
     * <p>
     * Performance Considerations:
     * - Current: Individual save() calls
     * - Production: Use saveAll() for batch processing
     * - Trade-off: Individual error handling vs batch performance
     */
    @Override
    @Transactional
    public int promotePendingOrdersToProcessing() {
        long startTime = System.currentTimeMillis();

        log.info("Starting promotion of PENDING orders to PROCESSING");

        List<Order> pendingOrders = orderRepository.findAllPendingOrders();

        if (pendingOrders.isEmpty()) {
            log.info("No PENDING orders found to promote");
            return 0;
        }

        log.info("Found {} PENDING orders to promote", pendingOrders.size());

        int successCount = 0;
        int failureCount = 0;

        for (Order order : pendingOrders) {
            try {
                // Update status
                order.setStatus(OrderStatus.PROCESSING);
                orderRepository.save(order);
                successCount++;

                log.debug("Promoted order {} to PROCESSING", order.getId());

            } catch (Exception e) {
                failureCount++;
                // Log error but continue with other orders
                log.error("Failed to promote order {} to PROCESSING - Error: {}",
                        order.getId(), e.getMessage(), e);
            }
        }

        long duration = System.currentTimeMillis() - startTime;

        log.info("Promotion completed - Success: {}, Failed: {}, Duration: {}ms",
                successCount, failureCount, duration);

        // Alert on high failure rate
        if (failureCount > 0) {
            double failureRate = (double) failureCount / pendingOrders.size();
            if (failureRate > 0.1) { // More than 10% failures
                log.error("HIGH FAILURE RATE in order promotion: {}/{} failed ({}%)",
                        failureCount, pendingOrders.size(),
                        String.format("%.2f", failureRate * 100));
            }
        }

        return successCount;
    }


    /**
     * Private helper: Validate order request
     * <p>
     * Multi-layer Validation Strategy:
     * - Layer 1: Bean Validation (@Valid) - structural constraints
     * - Layer 2: This method - business rules
     * - Layer 3: Database constraints - data integrity
     * <p>
     * Why private?
     * - Internal implementation detail
     * - Not part of public API
     * - Can be refactored without breaking contract
     */
    private void validateOrderRequest(OrderRequestDTO request) {
        // Defense in depth: Check items (should be caught by @NotEmpty)
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw OrderValidationException.emptyOrder();
        }
    }
}
