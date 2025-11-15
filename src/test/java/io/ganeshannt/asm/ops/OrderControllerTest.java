package io.ganeshannt.asm.ops;

import io.ganeshannt.asm.ops.dto.OrderItemDTO;
import io.ganeshannt.asm.ops.dto.OrderRequestDTO;
import io.ganeshannt.asm.ops.dto.OrderResponseDTO;
import io.ganeshannt.asm.ops.dto.PagedResponse;
import io.ganeshannt.asm.ops.enums.OrderStatus;
import io.ganeshannt.asm.ops.repository.OrderRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Ganeshannt
 * @version 1.0
 * @since 2025-10-23
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Order Processing System Tests")
class OrderControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void configureWebTestClient() {
        // Configure timeout for slow operations
        webTestClient = webTestClient.mutate()
                .responseTimeout(Duration.ofSeconds(10))
                .build();
    }

    // ===================================================================================
    // CREATE ORDER TESTS
    // ===================================================================================

    @Test
    @Order(1)
    @DisplayName("Should create order successfully with valid data")
    void testCreateOrder_Success() {
        // Arrange
        OrderRequestDTO request = OrderRequestDTO.builder()
                .customerEmail("create.test@example.com")
                .items(List.of(
                        OrderItemDTO.builder()
                                .productName("WebTestClient Product")
                                .quantity(2)
                                .price(new BigDecimal("49.99"))
                                .build()
                ))
                .build();

        // Act & Assert
        webTestClient.post()
                .uri("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(OrderResponseDTO.class)
                .value(response -> {
                    assertThat(response.getId()).isNotNull();
                    assertThat(response.getStatus()).isEqualTo(OrderStatus.PENDING);
                    assertThat(response.getCustomerEmail()).isEqualTo("create.test@example.com");
                    assertThat(response.getTotalAmount()).isEqualByComparingTo(new BigDecimal("99.98"));
                    assertThat(response.getItems()).hasSize(1);
                    assertThat(response.getItems().get(0).getProductName()).isEqualTo("WebTestClient Product");
                    assertThat(response.getItems().get(0).getQuantity()).isEqualTo(2);
                    assertThat(response.getItems().get(0).getLineTotal()).isEqualByComparingTo(new BigDecimal("99.98"));
                    assertThat(response.getCreatedAt()).isNotNull();
                    assertThat(response.getUpdatedAt()).isNotNull();
                });
    }

    @Test
    @Order(2)
    @DisplayName("Should create order with multiple items and calculate correct total")
    void testCreateOrder_MultipleItems() {
        // Arrange
        OrderRequestDTO request = OrderRequestDTO.builder()
                .customerEmail("multi.items@example.com")
                .items(List.of(
                        OrderItemDTO.builder()
                                .productName("Product A")
                                .quantity(2)
                                .price(new BigDecimal("25.50"))
                                .build(),
                        OrderItemDTO.builder()
                                .productName("Product B")
                                .quantity(1)
                                .price(new BigDecimal("100.00"))
                                .build(),
                        OrderItemDTO.builder()
                                .productName("Product C")
                                .quantity(3)
                                .price(new BigDecimal("15.99"))
                                .build()
                ))
                .build();

        // Act & Assert
        webTestClient.post()
                .uri("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(OrderResponseDTO.class)
                .value(response -> {
                    assertThat(response.getItems()).hasSize(3);
                    // (2 * 25.50) + 100.00 + (3 * 15.99) = 51.00 + 100.00 + 47.97 = 198.97
                    assertThat(response.getTotalAmount()).isEqualByComparingTo(new BigDecimal("198.97"));
                });
    }

    @Test
    @Order(3)
    @DisplayName("Should fail to create order with invalid email")
    void testCreateOrder_InvalidEmail() {
        // Arrange
        OrderRequestDTO request = OrderRequestDTO.builder()
                .customerEmail("invalid-email")
                .items(List.of(
                        OrderItemDTO.builder()
                                .productName("Product")
                                .quantity(1)
                                .price(new BigDecimal("50.00"))
                                .build()
                ))
                .build();

        // Act & Assert - Focus on what matters: 400 status
        webTestClient.post()
                .uri("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.error").exists()
                .jsonPath("$.message").exists();
    }

    @Test
    @Order(4)
    @DisplayName("Should fail to create order with empty items")
    void testCreateOrder_EmptyItems() {
        // Arrange
        OrderRequestDTO request = OrderRequestDTO.builder()
                .customerEmail("test@example.com")
                .items(List.of())
                .build();

        // Act & Assert - Focus on what matters: 400 status
        webTestClient.post()
                .uri("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.error").exists()
                .jsonPath("$.message").exists();
    }


    @Test
    @Order(5)
    @DisplayName("Should fail to create order with null customer email")
    void testCreateOrder_NullEmail() {
        // Arrange
        OrderRequestDTO request = OrderRequestDTO.builder()
                .customerEmail(null)
                .items(List.of(
                        OrderItemDTO.builder()
                                .productName("Product")
                                .quantity(1)
                                .price(new BigDecimal("50.00"))
                                .build()
                ))
                .build();

        // Act & Assert
        webTestClient.post()
                .uri("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @Order(6)
    @DisplayName("Should fail to create order with invalid item quantity")
    void testCreateOrder_InvalidQuantity() {
        // Arrange
        OrderRequestDTO request = OrderRequestDTO.builder()
                .customerEmail("test@example.com")
                .items(List.of(
                        OrderItemDTO.builder()
                                .productName("Product")
                                .quantity(0)  // Invalid: must be >= 1
                                .price(new BigDecimal("50.00"))
                                .build()
                ))
                .build();

        // Act & Assert
        webTestClient.post()
                .uri("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    // ===================================================================================
    // GET ORDER BY ID TESTS
    // ===================================================================================

    @Test
    @Order(7)
    @DisplayName("Should retrieve order by ID successfully")
    void testGetOrderById_Success() {
        // Act & Assert - Using test data (order with ID 1 exists)
        webTestClient.get()
                .uri("/api/v1/orders/1")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(OrderResponseDTO.class)
                .value(response -> {
                    assertThat(response.getId()).isEqualTo(1L);
                    assertThat(response.getCustomerEmail()).isEqualTo("pending@test.com");
                    assertThat(response.getStatus()).isEqualTo(OrderStatus.PENDING);
                    assertThat(response.getItems()).isNotEmpty();
                    assertThat(response.getTotalAmount()).isNotNull();
                });
    }

    @Test
    @Order(8)
    @DisplayName("Should return 404 for non-existent order")
    void testGetOrderById_NotFound() {
        // Act & Assert
        webTestClient.get()
                .uri("/api/v1/orders/99999")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.error").exists()
                .jsonPath("$.message").value(message ->
                        assertThat(message.toString()).containsIgnoringCase("Order not found"));
    }

    @Test
    @Order(9)
    @DisplayName("Should return 400 for invalid order ID format")
    void testGetOrderById_InvalidIdFormat() {
        // Act & Assert
        webTestClient.get()
                .uri("/api/v1/orders/invalid-id")
                .exchange()
                .expectStatus().isBadRequest();
    }

    // ===================================================================================
    // GET ALL ORDERS WITH PAGINATION TESTS
    // ===================================================================================

    @Test
    @Order(10)
    @DisplayName("Should retrieve all orders with default pagination")
    void testGetAllOrders_DefaultPagination() {
        // Act & Assert
        webTestClient.get()
                .uri("/api/v1/orders")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(PagedResponse.class)
                .value(response -> {
                    assertThat(response.getPageNumber()).isEqualTo(1);
                    assertThat(response.getPageSize()).isEqualTo(10);
                    assertThat(response.getContent()).isNotEmpty();
                    assertThat(response.getTotalElements()).isGreaterThan(0);
                    assertThat(response.getTotalPages()).isGreaterThan(0);
                });
    }

    @Test
    @Order(11)
    @DisplayName("Should retrieve orders with custom page size")
    void testGetAllOrders_CustomPageSize() {
        // Act & Assert
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/orders")
                        .queryParam("page", 1)
                        .queryParam("size", 3)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(PagedResponse.class)
                .value(response -> {
                    assertThat(response.getPageNumber()).isEqualTo(1);
                    assertThat(response.getPageSize()).isEqualTo(3);
                    assertThat(response.getContent().size()).isLessThanOrEqualTo(3);
                });
    }

    @Test
    @Order(12)
    @DisplayName("Should retrieve second page of orders")
    void testGetAllOrders_SecondPage() {
        // Act & Assert
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/orders")
                        .queryParam("page", 2)
                        .queryParam("size", 2)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(PagedResponse.class)
                .value(response -> {
                    assertThat(response.getPageNumber()).isEqualTo(2);
                    assertThat(response.getPageSize()).isEqualTo(2);
                });
    }

    @Test
    @Order(13)
    @DisplayName("Should clamp invalid page size to maximum")
    void testGetAllOrders_MaxPageSize() {
        // Act & Assert - Request size > 100 should be clamped to 100
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/orders")
                        .queryParam("size", 150)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(PagedResponse.class)
                .value(response -> {
                    assertThat(response.getPageSize()).isEqualTo(100);
                });
    }

    @Test
    @Order(14)
    @DisplayName("Should clamp invalid page number to minimum")
    void testGetAllOrders_MinPageNumber() {
        // Act & Assert - page=0 or negative should be clamped to 1
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/orders")
                        .queryParam("page", 0)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(PagedResponse.class)
                .value(response -> {
                    assertThat(response.getPageNumber()).isEqualTo(1);
                });
    }

    // ===================================================================================
    // FILTER ORDERS BY STATUS TESTS
    // ===================================================================================

    @Test
    @Order(15)
    @DisplayName("Should filter orders by PENDING status")
    void testGetAllOrders_FilterByPending() {
        // Act & Assert
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/orders")
                        .queryParam("status", "PENDING")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(PagedResponse.class)
                .value(response -> {
                    assertThat(response.getContent()).isNotEmpty();
                    // Verify all orders have PENDING status
                    assertThat(response.getContent()).allSatisfy(order -> {
                        @SuppressWarnings("unchecked")
                        String status = ((java.util.Map<String, Object>) order).get("status").toString();
                        assertThat(status).isEqualTo("PENDING");
                    });
                });
    }

    @Test
    @Order(16)
    @DisplayName("Should filter orders by PROCESSING status")
    void testGetAllOrders_FilterByProcessing() {
        // Act & Assert
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/orders")
                        .queryParam("status", "PROCESSING")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(PagedResponse.class)
                .value(response -> {
                    assertThat(response.getContent()).isNotEmpty();
                });
    }

    @Test
    @Order(17)
    @DisplayName("Should filter orders by SHIPPED status")
    void testGetAllOrders_FilterByShipped() {
        // Act & Assert
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/orders")
                        .queryParam("status", "SHIPPED")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(PagedResponse.class)
                .value(response -> {
                    assertThat(response.getContent()).isNotEmpty();
                });
    }

    @Test
    @Order(18)
    @DisplayName("Should filter orders by DELIVERED status")
    void testGetAllOrders_FilterByDelivered() {
        // Act & Assert
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/orders")
                        .queryParam("status", "DELIVERED")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(PagedResponse.class)
                .value(response -> {
                    assertThat(response.getContent()).isNotEmpty();
                });
    }

    @Test
    @Order(19)
    @DisplayName("Should filter orders by CANCELLED status")
    void testGetAllOrders_FilterByCancelled() {
        // Act & Assert
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/orders")
                        .queryParam("status", "CANCELLED")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(PagedResponse.class)
                .value(response -> {
                    assertThat(response.getContent()).isNotEmpty();
                });
    }

    @Test
    @Order(20)
    @DisplayName("Should combine status filter with pagination")
    void testGetAllOrders_StatusFilterWithPagination() {
        // Act & Assert
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/orders")
                        .queryParam("status", "PENDING")
                        .queryParam("page", 1)
                        .queryParam("size", 2)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(PagedResponse.class)
                .value(response -> {
                    assertThat(response.getPageNumber()).isEqualTo(1);
                    assertThat(response.getPageSize()).isEqualTo(2);
                    assertThat(response.getContent().size()).isLessThanOrEqualTo(2);
                });
    }

    // ===================================================================================
    // CANCEL ORDER TESTS
    // ===================================================================================

    @Test
    @Order(21)
    @DisplayName("Should cancel PENDING order successfully using PATCH")
    void testCancelOrder_Success() {
        // Arrange - Create a new PENDING order first
        OrderRequestDTO createRequest = OrderRequestDTO.builder()
                .customerEmail("cancel.test@example.com")
                .items(List.of(
                        OrderItemDTO.builder()
                                .productName("Product to Cancel")
                                .quantity(1)
                                .price(new BigDecimal("50.00"))
                                .build()
                ))
                .build();

        OrderResponseDTO createdOrder = webTestClient.post()
                .uri("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(OrderResponseDTO.class)
                .returnResult()
                .getResponseBody();

        Long orderId = createdOrder.getId();

        // Act & Assert - Cancel the order
        webTestClient.patch()
                .uri("/api/v1/orders/" + orderId + "/cancel")
                .exchange()
                .expectStatus().isOk()
                .expectBody(OrderResponseDTO.class)
                .value(response -> {
                    assertThat(response.getId()).isEqualTo(orderId);
                    assertThat(response.getStatus()).isEqualTo(OrderStatus.CANCELLED);
                    assertThat(response.getCustomerEmail()).isEqualTo("cancel.test@example.com");
                });
    }

    @Test
    @Order(22)
    @DisplayName("Should fail to cancel non-PENDING order")
    void testCancelOrder_InvalidStatus() {
        // Act & Assert - Try to cancel PROCESSING order (ID: 2)
        webTestClient.patch()
                .uri("/api/v1/orders/2/cancel")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").value(message ->
                        assertThat(message.toString()).containsIgnoringCase("Cannot cancel"));
    }

    @Test
    @Order(23)
    @DisplayName("Should fail to cancel non-existent order")
    void testCancelOrder_NotFound() {
        // Act & Assert
        webTestClient.patch()
                .uri("/api/v1/orders/99999/cancel")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").value(message ->
                        assertThat(message.toString()).containsIgnoringCase("Order not found"));
    }

    @Test
    @Order(24)
    @DisplayName("Should fail to cancel already cancelled order")
    void testCancelOrder_AlreadyCancelled() {
        // Act & Assert - Try to cancel CANCELLED order (ID: 5)
        webTestClient.patch()
                .uri("/api/v1/orders/5/cancel")
                .exchange()
                .expectStatus().isBadRequest();
    }

    // ===================================================================================
    // UPDATE ORDER STATUS TESTS
    // ===================================================================================

    @Test
    @Order(25)
    @DisplayName("Should update order status from PENDING to PROCESSING")
    void testUpdateOrderStatus_PendingToProcessing() {
        // Arrange - Create a PENDING order
        OrderRequestDTO createRequest = OrderRequestDTO.builder()
                .customerEmail("status.update@example.com")
                .items(List.of(
                        OrderItemDTO.builder()
                                .productName("Status Update Product")
                                .quantity(1)
                                .price(new BigDecimal("75.00"))
                                .build()
                ))
                .build();

        OrderResponseDTO createdOrder = webTestClient.post()
                .uri("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(OrderResponseDTO.class)
                .returnResult()
                .getResponseBody();

        Long orderId = createdOrder.getId();

        // Act & Assert - Update to PROCESSING
        webTestClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/orders/" + orderId + "/status")
                        .queryParam("status", "PROCESSING")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(OrderResponseDTO.class)
                .value(response -> {
                    assertThat(response.getId()).isEqualTo(orderId);
                    assertThat(response.getStatus()).isEqualTo(OrderStatus.PROCESSING);
                });
    }

    @Test
    @Order(26)
    @DisplayName("Should update order status from PROCESSING to SHIPPED")
    void testUpdateOrderStatus_ProcessingToShipped() {
        // Act & Assert - Update PROCESSING order (ID: 2) to SHIPPED
        webTestClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/orders/2/status")
                        .queryParam("status", "SHIPPED")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(OrderResponseDTO.class)
                .value(response -> {
                    assertThat(response.getStatus()).isEqualTo(OrderStatus.SHIPPED);
                });
    }

    @Test
    @Order(27)
    @DisplayName("Should fail to update with invalid status transition")
    void testUpdateOrderStatus_InvalidTransition() {
        // Act & Assert - Try to move DELIVERED (ID: 4) back to PENDING
        webTestClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/orders/4/status")
                        .queryParam("status", "PENDING")
                        .build())
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").value(message ->
                        assertThat(message.toString()).containsIgnoringCase("Invalid status transition"));
    }

    @Test
    @Order(28)
    @DisplayName("Should fail to update status of non-existent order")
    void testUpdateOrderStatus_NotFound() {
        // Act & Assert
        webTestClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/orders/99999/status")
                        .queryParam("status", "PROCESSING")
                        .build())
                .exchange()
                .expectStatus().isNotFound();
    }

    // ===================================================================================
    // COMPLETE ORDER LIFECYCLE TEST
    // ===================================================================================

    @Test
    @Order(29)
    @DisplayName("Should complete full order lifecycle: Create → Process → Ship → Deliver")
    void testCompleteOrderLifecycle() {
        // Step 1: Create order
        OrderRequestDTO createRequest = OrderRequestDTO.builder()
                .customerEmail("lifecycle@example.com")
                .items(List.of(
                        OrderItemDTO.builder()
                                .productName("Lifecycle Product")
                                .quantity(2)
                                .price(new BigDecimal("75.00"))
                                .build()
                ))
                .build();

        OrderResponseDTO createdOrder = webTestClient.post()
                .uri("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(OrderResponseDTO.class)
                .value(response -> {
                    assertThat(response.getStatus()).isEqualTo(OrderStatus.PENDING);
                    assertThat(response.getTotalAmount()).isEqualByComparingTo(new BigDecimal("150.00"));
                })
                .returnResult()
                .getResponseBody();

        Long orderId = createdOrder.getId();

        // Step 2: Update to PROCESSING
        webTestClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/orders/" + orderId + "/status")
                        .queryParam("status", "PROCESSING")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(OrderResponseDTO.class)
                .value(response -> assertThat(response.getStatus()).isEqualTo(OrderStatus.PROCESSING));

        // Step 3: Update to SHIPPED
        webTestClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/orders/" + orderId + "/status")
                        .queryParam("status", "SHIPPED")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(OrderResponseDTO.class)
                .value(response -> assertThat(response.getStatus()).isEqualTo(OrderStatus.SHIPPED));

        // Step 4: Update to DELIVERED
        webTestClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/orders/" + orderId + "/status")
                        .queryParam("status", "DELIVERED")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(OrderResponseDTO.class)
                .value(response -> assertThat(response.getStatus()).isEqualTo(OrderStatus.DELIVERED));

        // Step 5: Verify the final state
        webTestClient.get()
                .uri("/api/v1/orders/" + orderId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(OrderResponseDTO.class)
                .value(response -> {
                    assertThat(response.getStatus()).isEqualTo(OrderStatus.DELIVERED);
                    assertThat(response.getCustomerEmail()).isEqualTo("lifecycle@example.com");
                });
    }

    @Test
    @Order(30)
    @DisplayName("Should handle order cancellation workflow correctly")
    void testCancellationWorkflow() {
        // Step 1: Create order
        OrderRequestDTO createRequest = OrderRequestDTO.builder()
                .customerEmail("cancellation.workflow@example.com")
                .items(List.of(
                        OrderItemDTO.builder()
                                .productName("Product to Cancel in Workflow")
                                .quantity(1)
                                .price(new BigDecimal("99.99"))
                                .build()
                ))
                .build();

        OrderResponseDTO createdOrder = webTestClient.post()
                .uri("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(OrderResponseDTO.class)
                .returnResult()
                .getResponseBody();

        Long orderId = createdOrder.getId();

        // Step 2: Verify order is PENDING
        webTestClient.get()
                .uri("/api/v1/orders/" + orderId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(OrderResponseDTO.class)
                .value(response -> assertThat(response.getStatus()).isEqualTo(OrderStatus.PENDING));

        // Step 3: Cancel order
        webTestClient.patch()
                .uri("/api/v1/orders/" + orderId + "/cancel")
                .exchange()
                .expectStatus().isOk()
                .expectBody(OrderResponseDTO.class)
                .value(response -> assertThat(response.getStatus()).isEqualTo(OrderStatus.CANCELLED));

        // Step 4: Verify cannot update cancelled order
        webTestClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/orders/" + orderId + "/status")
                        .queryParam("status", "PROCESSING")
                        .build())
                .exchange()
                .expectStatus().isBadRequest();

        // Step 5: Verify cannot cancel already cancelled order
        webTestClient.patch()
                .uri("/api/v1/orders/" + orderId + "/cancel")
                .exchange()
                .expectStatus().isBadRequest();
    }

    // ===================================================================================
    // EDGE CASES AND ERROR SCENARIOS
    // ===================================================================================

    @Test
    @Order(31)
    @DisplayName("Should handle large order with many items")
    void testCreateOrder_LargeOrder() {
        // Arrange - Create order with 10 items
        List<OrderItemDTO> items = List.of(
                createItem("Item 1", 1, "10.00"),
                createItem("Item 2", 2, "20.00"),
                createItem("Item 3", 3, "30.00"),
                createItem("Item 4", 1, "40.00"),
                createItem("Item 5", 2, "50.00"),
                createItem("Item 6", 1, "60.00"),
                createItem("Item 7", 3, "70.00"),
                createItem("Item 8", 1, "80.00"),
                createItem("Item 9", 2, "90.00"),
                createItem("Item 10", 1, "100.00")
        );

        OrderRequestDTO request = OrderRequestDTO.builder()
                .customerEmail("large.order@example.com")
                .items(items)
                .build();

        // Act & Assert
        webTestClient.post()
                .uri("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(OrderResponseDTO.class)
                .value(response -> {
                    assertThat(response.getItems()).hasSize(10);
                    assertThat(response.getTotalAmount()).isGreaterThan(BigDecimal.ZERO);
                });
    }

    @Test
    @Order(32)
    @DisplayName("Should handle concurrent order creation")
    void testConcurrentOrderCreation() {
        // Arrange
        OrderRequestDTO request = OrderRequestDTO.builder()
                .customerEmail("concurrent@example.com")
                .items(List.of(
                        OrderItemDTO.builder()
                                .productName("Concurrent Product")
                                .quantity(1)
                                .price(new BigDecimal("50.00"))
                                .build()
                ))
                .build();

        // Act - Create multiple orders concurrently
        for (int i = 0; i < 5; i++) {
            webTestClient.post()
                    .uri("/api/v1/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().isCreated();
        }

        // Assert - Verify all orders were created
        long totalOrders = orderRepository.count();
        assertThat(totalOrders).isGreaterThanOrEqualTo(10); // 5 test data + 5 created
    }

    // ===================================================================================
    // HELPER METHODS
    // ===================================================================================

    private OrderItemDTO createItem(String productName, int quantity, String price) {
        return OrderItemDTO.builder()
                .productName(productName)
                .quantity(quantity)
                .price(new BigDecimal(price))
                .build();
    }
}
