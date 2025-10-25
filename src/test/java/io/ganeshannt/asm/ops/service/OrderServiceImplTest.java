package io.ganeshannt.asm.ops.service;

import io.ganeshannt.asm.ops.dto.OrderItemDTO;
import io.ganeshannt.asm.ops.dto.OrderRequestDTO;
import io.ganeshannt.asm.ops.dto.OrderResponseDTO;
import io.ganeshannt.asm.ops.entity.Order;
import io.ganeshannt.asm.ops.entity.OrderItem;
import io.ganeshannt.asm.ops.enums.OrderStatus;
import io.ganeshannt.asm.ops.exception.InvalidOrderStatusException;
import io.ganeshannt.asm.ops.exception.OrderNotFoundException;
import io.ganeshannt.asm.ops.exception.OrderValidationException;
import io.ganeshannt.asm.ops.mapper.OrderMapper;
import io.ganeshannt.asm.ops.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Comprehensive Unit Tests for OrderServiceImpl
 * @author Ganeshannt
 * @version 1.0
 * @since 2025-10-25
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderServiceImpl Unit Tests")
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order testOrder;
    private OrderRequestDTO testOrderRequest;
    private OrderResponseDTO testOrderResponse;
    private OrderItem testOrderItem;
    private OrderItemDTO testOrderItemDTO;

    @BeforeEach
    void setUp() {
        // Setup test order item
        testOrderItem = OrderItem.builder()
                .id(1L)
                .productName("Test Product")
                .quantity(2)
                .price(new BigDecimal("50.00"))
                .build();

        // Setup test order
        testOrder = Order.builder()
                .id(1L)
                .customerEmail("test@example.com")
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("100.00"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        testOrder.addItem(testOrderItem);

        // Setup test DTOs
        testOrderItemDTO = OrderItemDTO.builder()
                .productName("Test Product")
                .quantity(2)
                .price(new BigDecimal("50.00"))
                .build();

        testOrderRequest = OrderRequestDTO.builder()
                .customerEmail("test@example.com")
                .items(List.of(testOrderItemDTO))
                .build();

        testOrderResponse = OrderResponseDTO.builder()
                .id(1L)
                .customerEmail("test@example.com")
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("100.00"))
                .items(List.of(testOrderItemDTO))
                .build();
    }

    // ===================================================================================
    // CREATE ORDER TESTS
    // ===================================================================================

    @Nested
    @DisplayName("Create Order Tests")
    class CreateOrderTests {

        @Test
        @DisplayName("Should create order successfully with valid data")
        void testCreateOrder_Success() {
            // Arrange
            when(orderMapper.toEntity(testOrderRequest)).thenReturn(testOrder);
            when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
            when(orderMapper.toResponseDTO(testOrder)).thenReturn(testOrderResponse);

            // Act
            OrderResponseDTO result = orderService.createOrder(testOrderRequest);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getCustomerEmail()).isEqualTo("test@example.com");
            assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);
            assertThat(result.getTotalAmount()).isEqualByComparingTo(new BigDecimal("100.00"));

            // Verify interactions
            verify(orderMapper, times(1)).toEntity(testOrderRequest);
            verify(orderRepository, times(1)).save(any(Order.class));
            verify(orderMapper, times(1)).toResponseDTO(testOrder);
        }

        @Test
        @DisplayName("Should throw exception when total amount is zero")
        void testCreateOrder_ZeroTotalAmount() {
            // Arrange
            Order orderWithZeroTotal = Order.builder()
                    .customerEmail("test@example.com")
                    .status(OrderStatus.PENDING)
                    .totalAmount(BigDecimal.ZERO)
                    .build();

            when(orderMapper.toEntity(testOrderRequest)).thenReturn(orderWithZeroTotal);

            // Act & Assert
            assertThatThrownBy(() -> orderService.createOrder(testOrderRequest))
                    .isInstanceOf(OrderValidationException.class)
                    .hasMessageContaining("total");

            // Verify save was not called
            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        @DisplayName("Should throw exception when total amount is negative")
        void testCreateOrder_NegativeTotalAmount() {
            // Arrange
            Order orderWithNegativeTotal = Order.builder()
                    .customerEmail("test@example.com")
                    .status(OrderStatus.PENDING)
                    .totalAmount(new BigDecimal("-10.00"))
                    .build();

            when(orderMapper.toEntity(testOrderRequest)).thenReturn(orderWithNegativeTotal);

            // Act & Assert
            assertThatThrownBy(() -> orderService.createOrder(testOrderRequest))
                    .isInstanceOf(OrderValidationException.class);

            verify(orderRepository, never()).save(any(Order.class));
        }
    }

    // ===================================================================================
    // GET ORDER BY ID TESTS
    // ===================================================================================

    @Nested
    @DisplayName("Get Order by ID Tests")
    class GetOrderByIdTests {

        @Test
        @DisplayName("Should retrieve order by ID successfully")
        void testGetOrderById_Success() {
            // Arrange
            when(orderRepository.findByIdWithItems(1L)).thenReturn(Optional.of(testOrder));
            when(orderMapper.toResponseDTO(testOrder)).thenReturn(testOrderResponse);

            // Act
            OrderResponseDTO result = orderService.getOrderById(1L);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getCustomerEmail()).isEqualTo("test@example.com");

            verify(orderRepository, times(1)).findByIdWithItems(1L);
            verify(orderMapper, times(1)).toResponseDTO(testOrder);
        }

        @Test
        @DisplayName("Should throw exception when order not found")
        void testGetOrderById_NotFound() {
            // Arrange
            when(orderRepository.findByIdWithItems(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> orderService.getOrderById(999L))
                    .isInstanceOf(OrderNotFoundException.class)
                    .hasMessageContaining("Order not found")
                    .hasMessageContaining("999");

            verify(orderRepository, times(1)).findByIdWithItems(999L);
            verify(orderMapper, never()).toResponseDTO(any());
        }

        @Test
        @DisplayName("Should handle null ID gracefully")
        void testGetOrderById_NullId() {
            // Arrange
            when(orderRepository.findByIdWithItems(null)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> orderService.getOrderById(null))
                    .isInstanceOf(OrderNotFoundException.class);

            verify(orderRepository, times(1)).findByIdWithItems(null);
        }
    }

    // ===================================================================================
    // GET ALL ORDERS TESTS
    // ===================================================================================

    @Nested
    @DisplayName("Get All Orders Tests")
    class GetAllOrdersTests {

        @Test
        @DisplayName("Should retrieve all orders without status filter")
        void testGetAllOrders_NoFilter() {
            // Arrange
            List<Order> orders = List.of(testOrder);
            Page<Order> orderPage = new PageImpl<>(orders);
            Pageable pageable = PageRequest.of(0, 10);

            when(orderRepository.findAll(pageable)).thenReturn(orderPage);
            when(orderMapper.toResponseDTO(testOrder)).thenReturn(testOrderResponse);

            // Act
            Page<OrderResponseDTO> result = orderService.getAllOrders(null, pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getId()).isEqualTo(1L);

            verify(orderRepository, times(1)).findAll(pageable);
            verify(orderRepository, never()).findByStatus(any(), any());
        }

        @Test
        @DisplayName("Should retrieve orders filtered by PENDING status")
        void testGetAllOrders_WithStatusFilter() {
            // Arrange
            List<Order> orders = List.of(testOrder);
            Page<Order> orderPage = new PageImpl<>(orders);
            Pageable pageable = PageRequest.of(0, 10);

            when(orderRepository.findByStatus(OrderStatus.PENDING, pageable)).thenReturn(orderPage);
            when(orderMapper.toResponseDTO(testOrder)).thenReturn(testOrderResponse);

            // Act
            Page<OrderResponseDTO> result = orderService.getAllOrders(OrderStatus.PENDING, pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getStatus()).isEqualTo(OrderStatus.PENDING);

            verify(orderRepository, times(1)).findByStatus(OrderStatus.PENDING, pageable);
            verify(orderRepository, never()).findAll(any(Pageable.class));
        }

        @Test
        @DisplayName("Should return empty page when no orders found")
        void testGetAllOrders_EmptyResult() {
            // Arrange
            Page<Order> emptyPage = new PageImpl<>(List.of());
            Pageable pageable = PageRequest.of(0, 10);

            when(orderRepository.findAll(pageable)).thenReturn(emptyPage);

            // Act
            Page<OrderResponseDTO> result = orderService.getAllOrders(null, pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();

            verify(orderRepository, times(1)).findAll(pageable);
        }

        @Test
        @DisplayName("Should handle pagination correctly")
        void testGetAllOrders_Pagination() {
            // Arrange
            List<Order> orders = List.of(testOrder);
            Page<Order> orderPage = new PageImpl<>(orders, PageRequest.of(1, 5), 15);
            Pageable pageable = PageRequest.of(1, 5);

            when(orderRepository.findAll(pageable)).thenReturn(orderPage);
            when(orderMapper.toResponseDTO(testOrder)).thenReturn(testOrderResponse);

            // Act
            Page<OrderResponseDTO> result = orderService.getAllOrders(null, pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getNumber()).isEqualTo(1);
            assertThat(result.getSize()).isEqualTo(5);
            assertThat(result.getTotalElements()).isEqualTo(15);
            assertThat(result.getTotalPages()).isEqualTo(3);
        }
    }

    // ===================================================================================
    // CANCEL ORDER TESTS
    // ===================================================================================

    @Nested
    @DisplayName("Cancel Order Tests")
    class CancelOrderTests {

        @Test
        @DisplayName("Should cancel PENDING order successfully")
        void testCancelOrder_Success() {
            // Arrange
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
            when(orderMapper.toResponseDTO(any(Order.class))).thenReturn(testOrderResponse);

            // Act
            OrderResponseDTO result = orderService.cancelOrder(1L);

            // Assert
            assertThat(result).isNotNull();
            verify(orderRepository, times(1)).findById(1L);
            verify(orderRepository, times(1)).save(any(Order.class));
        }

        @Test
        @DisplayName("Should throw exception when cancelling non-PENDING order")
        void testCancelOrder_InvalidStatus() {
            // Arrange
            Order processingOrder = Order.builder()
                    .id(2L)
                    .status(OrderStatus.PROCESSING)
                    .customerEmail("test@example.com")
                    .totalAmount(new BigDecimal("100.00"))
                    .build();

            when(orderRepository.findById(2L)).thenReturn(Optional.of(processingOrder));

            // Act & Assert
            assertThatThrownBy(() -> orderService.cancelOrder(2L))
                    .isInstanceOf(InvalidOrderStatusException.class)
                    .hasMessageContaining("Cannot cancel");

            verify(orderRepository, times(1)).findById(2L);
            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        @DisplayName("Should throw exception when order not found")
        void testCancelOrder_NotFound() {
            // Arrange
            when(orderRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> orderService.cancelOrder(999L))
                    .isInstanceOf(OrderNotFoundException.class)
                    .hasMessageContaining("Order not found");

            verify(orderRepository, times(1)).findById(999L);
            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        @DisplayName("Should throw exception when cancelling DELIVERED order")
        void testCancelOrder_DeliveredOrder() {
            // Arrange
            Order deliveredOrder = Order.builder()
                    .id(3L)
                    .status(OrderStatus.DELIVERED)
                    .customerEmail("test@example.com")
                    .totalAmount(new BigDecimal("100.00"))
                    .build();

            when(orderRepository.findById(3L)).thenReturn(Optional.of(deliveredOrder));

            // Act & Assert
            assertThatThrownBy(() -> orderService.cancelOrder(3L))
                    .isInstanceOf(InvalidOrderStatusException.class);

            verify(orderRepository, never()).save(any(Order.class));
        }
    }

    // ===================================================================================
    // UPDATE ORDER STATUS TESTS
    // ===================================================================================

    @Nested
    @DisplayName("Update Order Status Tests")
    class UpdateOrderStatusTests {

        @Test
        @DisplayName("Should update status from PENDING to PROCESSING")
        void testUpdateOrderStatus_PendingToProcessing() {
            // Arrange
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
            when(orderMapper.toResponseDTO(any(Order.class))).thenReturn(testOrderResponse);

            // Act
            OrderResponseDTO result = orderService.updateOrderStatus(1L, OrderStatus.PROCESSING);

            // Assert
            assertThat(result).isNotNull();
            verify(orderRepository, times(1)).findById(1L);
            verify(orderRepository, times(1)).save(any(Order.class));
        }

        @Test
        @DisplayName("Should throw exception for invalid status transition")
        void testUpdateOrderStatus_InvalidTransition() {
            // Arrange
            Order deliveredOrder = Order.builder()
                    .id(4L)
                    .status(OrderStatus.DELIVERED)
                    .customerEmail("test@example.com")
                    .totalAmount(new BigDecimal("100.00"))
                    .build();

            when(orderRepository.findById(4L)).thenReturn(Optional.of(deliveredOrder));

            // Act & Assert
            assertThatThrownBy(() -> orderService.updateOrderStatus(4L, OrderStatus.PENDING))
                    .isInstanceOf(InvalidOrderStatusException.class)
                    .hasMessageContaining("Invalid status transition");

            verify(orderRepository, times(1)).findById(4L);
            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        @DisplayName("Should throw exception when order not found")
        void testUpdateOrderStatus_NotFound() {
            // Arrange
            when(orderRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> orderService.updateOrderStatus(999L, OrderStatus.PROCESSING))
                    .isInstanceOf(OrderNotFoundException.class);

            verify(orderRepository, times(1)).findById(999L);
            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        @DisplayName("Should update status from PROCESSING to SHIPPED")
        void testUpdateOrderStatus_ProcessingToShipped() {
            // Arrange
            Order processingOrder = Order.builder()
                    .id(5L)
                    .status(OrderStatus.PROCESSING)
                    .customerEmail("test@example.com")
                    .totalAmount(new BigDecimal("100.00"))
                    .build();

            when(orderRepository.findById(5L)).thenReturn(Optional.of(processingOrder));
            when(orderRepository.save(any(Order.class))).thenReturn(processingOrder);
            when(orderMapper.toResponseDTO(any(Order.class))).thenReturn(testOrderResponse);

            // Act
            OrderResponseDTO result = orderService.updateOrderStatus(5L, OrderStatus.SHIPPED);

            // Assert
            assertThat(result).isNotNull();
            verify(orderRepository, times(1)).save(any(Order.class));
        }
    }

    // ===================================================================================
    // PROMOTE PENDING ORDERS TESTS
    // ===================================================================================

    @Nested
    @DisplayName("Promote Pending Orders Tests")
    class PromotePendingOrdersTests {

        @Test
        @DisplayName("Should return 0 when no pending orders exist")
        void testPromotePendingOrders_NoPendingOrders() {
            // Arrange
            when(orderRepository.findAllPendingOrders()).thenReturn(List.of());

            // Act
            int result = orderService.promotePendingOrdersToProcessing();

            // Assert
            assertThat(result).isZero();
            verify(orderRepository, times(1)).findAllPendingOrders();
            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        @DisplayName("Should promote all pending orders successfully")
        void testPromotePendingOrders_Success() {
            // Arrange
            Order order1 = Order.builder()
                    .id(1L)
                    .status(OrderStatus.PENDING)
                    .customerEmail("test1@example.com")
                    .totalAmount(new BigDecimal("100.00"))
                    .build();

            Order order2 = Order.builder()
                    .id(2L)
                    .status(OrderStatus.PENDING)
                    .customerEmail("test2@example.com")
                    .totalAmount(new BigDecimal("200.00"))
                    .build();

            List<Order> pendingOrders = List.of(order1, order2);

            when(orderRepository.findAllPendingOrders()).thenReturn(pendingOrders);
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            int result = orderService.promotePendingOrdersToProcessing();

            // Assert
            assertThat(result).isEqualTo(2);
            verify(orderRepository, times(1)).findAllPendingOrders();
            verify(orderRepository, times(2)).save(any(Order.class));
        }

        @Test
        @DisplayName("Should handle partial failures during promotion")
        void testPromotePendingOrders_PartialFailure() {
            // Arrange
            Order order1 = Order.builder()
                    .id(1L)
                    .status(OrderStatus.PENDING)
                    .customerEmail("test1@example.com")
                    .totalAmount(new BigDecimal("100.00"))
                    .build();

            Order order2 = Order.builder()
                    .id(2L)
                    .status(OrderStatus.PENDING)
                    .customerEmail("test2@example.com")
                    .totalAmount(new BigDecimal("200.00"))
                    .build();

            List<Order> pendingOrders = List.of(order1, order2);

            when(orderRepository.findAllPendingOrders()).thenReturn(pendingOrders);
            when(orderRepository.save(any(Order.class)))
                    .thenReturn(order1)
                    .thenThrow(new RuntimeException("Database error"));

            // Act
            int result = orderService.promotePendingOrdersToProcessing();

            // Assert
            assertThat(result).isEqualTo(1); // Only 1 succeeded
            verify(orderRepository, times(1)).findAllPendingOrders();
            verify(orderRepository, times(2)).save(any(Order.class));
        }

        @Test
        @DisplayName("Should log high failure rate warning")
        void testPromotePendingOrders_HighFailureRate() {
            // Arrange
            List<Order> pendingOrders = new ArrayList<>();
            for (int i = 1; i <= 10; i++) {
                pendingOrders.add(Order.builder()
                        .id((long) i)
                        .status(OrderStatus.PENDING)
                        .customerEmail("test" + i + "@example.com")
                        .totalAmount(new BigDecimal("100.00"))
                        .build());
            }

            when(orderRepository.findAllPendingOrders()).thenReturn(pendingOrders);
            when(orderRepository.save(any(Order.class)))
                    .thenThrow(new RuntimeException("Database error"));

            // Act
            int result = orderService.promotePendingOrdersToProcessing();

            // Assert
            assertThat(result).isZero(); // All failed
            verify(orderRepository, times(10)).save(any(Order.class));
        }
    }
}
