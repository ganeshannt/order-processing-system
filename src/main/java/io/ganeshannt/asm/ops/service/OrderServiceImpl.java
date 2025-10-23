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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Order Service Implementation
 *
 * @author Ganeshannt
 * @version 1.2
 * @since 2025-10-23
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrderServiceImpl implements IOrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO request) {
        log.info("Creating order for customer: {} with {} items",
                request.getCustomerEmail(),
                request.getItems().size());

        validateOrderRequest(request);

        Order order = orderMapper.toEntity(request);

        if (order.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw OrderValidationException.invalidTotal();
        }

        Order savedOrder = orderRepository.save(order);

        log.info("Order created successfully - ID: {}, Total: {}, Status: {}",
                savedOrder.getId(),
                savedOrder.getTotalAmount(),
                savedOrder.getStatus());

        return orderMapper.toResponseDTO(savedOrder);
    }

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
     *
     * Implementation with Pagination Support
     *
     * Note: Items are loaded lazily within the @Transactional context
     * This avoids N+1 problem while supporting pagination
     */
    @Override
    public Page<OrderResponseDTO> getAllOrders(OrderStatus status, Pageable pageable) {
        log.debug("Fetching orders - Page: {}, Size: {}, Status: {}",
                pageable.getPageNumber() + 1,  // Log as 1-indexed
                pageable.getPageSize(),
                status);

        Page<Order> ordersPage;

        if (status != null) {
            // Filter by status with pagination
            // Use the overloaded findByStatus method that accepts Pageable
            ordersPage = orderRepository.findByStatus(status, pageable);
            log.debug("Found {} orders with status {} (page {} of {})",
                    ordersPage.getNumberOfElements(),
                    status,
                    ordersPage.getNumber() + 1,
                    ordersPage.getTotalPages());
        } else {
            // Get all orders with pagination
            ordersPage = orderRepository.findAll(pageable);
            log.debug("Found {} orders (page {} of {})",
                    ordersPage.getNumberOfElements(),
                    ordersPage.getNumber() + 1,
                    ordersPage.getTotalPages());
        }

        // Convert Page<Order> to Page<OrderResponseDTO>
        // Items will be loaded lazily during mapping (within transaction)
        return ordersPage.map(orderMapper::toResponseDTO);
    }

    @Override
    @Transactional
    public OrderResponseDTO cancelOrder(Long id) {
        log.info("Attempting to cancel order with ID: {}", id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> OrderNotFoundException.withId(id));

        if (!order.canBeCancelled()) {
            log.warn("Cannot cancel order {} - Current status: {}", id, order.getStatus());
            throw InvalidOrderStatusException.cannotCancel(order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order updatedOrder = orderRepository.save(order);

        log.info("Order {} cancelled successfully", id);
        return orderMapper.toResponseDTO(updatedOrder);
    }

    @Override
    @Transactional
    public OrderResponseDTO updateOrderStatus(Long id, OrderStatus newStatus) {
        log.info("Updating order {} to status: {}", id, newStatus);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> OrderNotFoundException.withId(id));

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
                order.setStatus(OrderStatus.PROCESSING);
                orderRepository.save(order);
                successCount++;

                log.debug("Promoted order {} to PROCESSING", order.getId());

            } catch (Exception e) {
                failureCount++;
                log.error("Failed to promote order {} to PROCESSING - Error: {}",
                        order.getId(), e.getMessage(), e);
            }
        }

        long duration = System.currentTimeMillis() - startTime;

        log.info("Promotion completed - Success: {}, Failed: {}, Duration: {}ms",
                successCount, failureCount, duration);

        if (failureCount > 0) {
            double failureRate = (double) failureCount / pendingOrders.size();
            if (failureRate > 0.1) {
                log.error("HIGH FAILURE RATE in order promotion: {}/{} failed ({}%)",
                        failureCount, pendingOrders.size(),
                        String.format("%.2f", failureRate * 100));
            }
        }

        return successCount;
    }

    private void validateOrderRequest(OrderRequestDTO request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw OrderValidationException.emptyOrder();
        }
    }
}
