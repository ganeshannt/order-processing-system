# Design Patterns and Principles Used in Order Processing System

A comprehensive guide to the software design patterns and architectural principles implemented in this project.

---

## Table of Contents

- [Design Patterns](#design-patterns)
    - [Creational Patterns](#creational-patterns)
    - [Structural Patterns](#structural-patterns)
    - [Behavioral Patterns](#behavioral-patterns)
- [Architectural Patterns](#architectural-patterns)
- [Design Principles](#design-principles)
- [Implementation Examples](#implementation-examples)

***

## Design Patterns

### Creational Patterns

Patterns that deal with object creation mechanisms.

#### 1. **Singleton Pattern**

**Description**: Ensures a class has only one instance and provides a global point of access to it.

**Purpose**:
- Controls object creation
- Ensures single instance across application
- Manages shared resources efficiently

**Implementation in Project**:
```java
@Service  // Spring manages as singleton by default
@RequiredArgsConstructor
public class OrderServiceImpl implements IOrderService {
    // Only one instance exists in Spring container
}
```

**Benefits**:
- Memory efficient (one instance)
- Consistent state management
- Thread-safe (when properly managed by Spring)
- Global access point

**When to Use**:
- Managing shared resources (database connections, configuration)
- Coordinating system-wide actions
- Logging, caching services

***

#### 2. **Builder Pattern**

**Description**: Separates object construction from its representation, allowing step-by-step creation of complex objects.

**Purpose**:
- Constructs complex objects step by step
- Produces different representations using same construction process
- Provides fluent API for object creation

**Implementation in Project**:
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private Long id;
    private String customerEmail;
    private OrderStatus status;
    private BigDecimal totalAmount;
}

// Usage
Order order = Order.builder()
    .customerEmail("test@example.com")
    .status(OrderStatus.PENDING)
    .totalAmount(new BigDecimal("100.00"))
    .build();
```

**Benefits**:
- Immutable objects
- Readable code
- No telescoping constructors
- Handles optional parameters elegantly

**When to Use**:
- Objects with many parameters
- Complex initialization logic
- When immutability is desired
- DTOs and domain models

***

#### 3. **Factory Pattern**

**Description**: Provides interface for creating objects without specifying exact classes.

**Purpose**:
- Encapsulates object creation logic
- Promotes loose coupling
- Centralizes object instantiation

**Implementation in Project**:
```java
// Exception Factory
public class OrderNotFoundException extends RuntimeException {
    public static OrderNotFoundException withId(Long id) {
        return new OrderNotFoundException(
            "Order not found with ID: " + id
        );
    }
}

// DTO Factory
public class PagedResponse<T> {
    public static <T> PagedResponse<T> of(Page<T> page) {
        return PagedResponse.<T>builder()
            .content(page.getContent())
            .pageNumber(page.getNumber() + 1)
            .pageSize(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .build();
    }
}
```

**Benefits**:
- Consistent object creation
- Easy to extend with new types
- Hides instantiation complexity
- Improves maintainability

**When to Use**:
- Complex object initialization
- Multiple object types with common interface
- Need to abstract creation logic

***

### Structural Patterns

Patterns that deal with object composition and relationships.

#### 4. **Repository Pattern**

**Description**: Mediates between domain and data mapping layers, acting like an in-memory collection of domain objects.

**Purpose**:
- Abstracts data access logic
- Centralizes database operations
- Provides collection-like interface

**Implementation in Project**:
```java
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByStatus(OrderStatus status);
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
    Optional<Order> findByIdWithItems(Long id);
    List<Order> findAllPendingOrders();
}
```

**Benefits**:
- Decouples business logic from data access
- Easy to test with mocking
- Centralized query logic
- Swap implementations easily

**When to Use**:
- Any application with database
- Need to abstract persistence
- Want testable data access

***

#### 5. **Adapter Pattern**

**Description**: Converts interface of a class into another interface clients expect, allowing incompatible interfaces to work together.

**Purpose**:
- Makes incompatible interfaces compatible
- Wraps existing class with new interface
- Allows reuse of existing functionality

**Implementation in Project**:
```java
public class PagedResponse<T> {
    // Adapts Spring's Page<T> (0-indexed) to custom format (1-indexed)
    public static <T> PagedResponse<T> of(Page<T> page) {
        return PagedResponse.<T>builder()
            .content(page.getContent())
            .pageNumber(page.getNumber() + 1)  // 0-indexed → 1-indexed
            .pageSize(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .build();
    }
}
```

**Benefits**:
- Adapts external libraries
- Clean client interface
- Hides implementation details
- Easy to switch libraries

**When to Use**:
- Integrating third-party libraries
- Legacy code integration
- API versioning

***

#### 6. **Facade Pattern**

**Description**: Provides unified, simplified interface to a set of interfaces in a subsystem.

**Purpose**:
- Simplifies complex subsystems
- Reduces dependencies on subsystem
- Provides higher-level interface

**Implementation in Project**:
```java
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    private final IOrderService orderService;
    
    // Facade: Hides complex service operations behind simple REST API
    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(
            @Valid @RequestBody OrderRequestDTO request) {
        return new ResponseEntity<>(
            orderService.createOrder(request), 
            HttpStatus.CREATED
        );
    }
}
```

**Benefits**:
- Simplified client interface
- Reduces complexity
- Loose coupling
- Easy to understand

**When to Use**:
- Complex subsystem with many classes
- Need simplified interface
- Want to decouple clients from subsystem

***

#### 7. **Proxy Pattern**

**Description**: Provides surrogate or placeholder for another object to control access to it.

**Purpose**:
- Controls access to object
- Adds additional behavior
- Lazy initialization support

**Implementation in Project**:
```java
@Service
public class OrderServiceImpl implements IOrderService {
    
    @Transactional  // Spring creates proxy for transaction management
    public OrderResponseDTO createOrder(OrderRequestDTO request) {
        // Proxy intercepts and:
        // 1. Begins transaction
        // 2. Executes method
        // 3. Commits/rollbacks transaction
        return orderMapper.toResponseDTO(savedOrder);
    }
}
```

**Benefits**:
- Adds behavior without modifying code
- Transaction management
- Security, logging, caching
- Lazy loading

**When to Use**:
- Need cross-cutting concerns
- Transaction management
- Security checks
- Caching

***

### Behavioral Patterns

Patterns that deal with object collaboration and responsibility distribution.

#### 8. **Strategy Pattern**

**Description**: Defines family of algorithms, encapsulates each one, and makes them interchangeable.

**Purpose**:
- Encapsulates algorithms
- Makes algorithms interchangeable
- Varies independently from clients

**Implementation in Project**:
```java
public enum OrderStatus {
    PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED;
    
    // Strategy: Different transition rules for each status
    public boolean canTransitionTo(OrderStatus newStatus) {
        return switch (this) {
            case PENDING -> newStatus == PROCESSING || newStatus == CANCELLED;
            case PROCESSING -> newStatus == SHIPPED || newStatus == CANCELLED;
            case SHIPPED -> newStatus == DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
    }
}
```

**Benefits**:
- Encapsulates business rules
- Easy to add new strategies
- Type-safe state management
- Self-documenting

**When to Use**:
- Multiple related classes differ only in behavior
- Need different variants of algorithm
- Want to hide complex logic

***

#### 9. **Template Method Pattern**

**Description**: Defines skeleton of algorithm, letting subclasses override specific steps without changing structure.

**Purpose**:
- Defines algorithm structure
- Allows customization of steps
- Code reuse through inheritance

**Implementation in Project**:
```java
@Service
public class OrderServiceImpl implements IOrderService {
    
    @Transactional  // Template: Spring manages transaction lifecycle
    public OrderResponseDTO createOrder(OrderRequestDTO request) {
        // Step 1: Spring begins transaction
        validateOrderRequest(request);           // Custom step
        Order order = orderMapper.toEntity(request);
        Order savedOrder = orderRepository.save(order);
        // Step 2: Spring commits/rollbacks transaction
        return orderMapper.toResponseDTO(savedOrder);
    }
}
```

**Benefits**:
- Code reuse
- Consistent algorithm structure
- Extensible
- Inversion of control

**When to Use**:
- Common algorithm with variations
- Want to prevent code duplication
- Need consistent framework

***

#### 10. **Observer Pattern**

**Description**: Defines one-to-many dependency so when one object changes state, dependents are notified automatically.

**Purpose**:
- Establishes subscription mechanism
- Notifies multiple objects of events
- Loose coupling between objects

**Implementation in Project**:
```java
@Component
@RequiredArgsConstructor
public class OrderStatusScheduler {
    private final IOrderService orderService;
    
    // Observer: Spring observes time and triggers action
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void promotePendingOrders() {
        orderService.promotePendingOrdersToProcessing();
    }
}
```

**Benefits**:
- Decoupled event handling
- Dynamic subscriptions
- Supports broadcast communication
- Open/closed principle

**When to Use**:
- State changes need to trigger actions
- Multiple objects need notification
- Event-driven architecture

***

#### 11. **Chain of Responsibility Pattern**

**Description**: Passes request along chain of handlers until one handles it.

**Purpose**:
- Decouples sender and receiver
- Multiple objects can handle request
- Dynamic handler chain

**Implementation in Project**:
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    // Handler 1: Specific exception
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            OrderNotFoundException ex) {
        return ResponseEntity.status(404).body(buildError(ex));
    }
    
    // Handler 2: Another specific exception
    @ExceptionHandler(InvalidOrderStatusException.class)
    public ResponseEntity<ErrorResponse> handleInvalidStatus(
            InvalidOrderStatusException ex) {
        return ResponseEntity.status(400).body(buildError(ex));
    }
    
    // Handler 3: Fallback for all exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        return ResponseEntity.status(500).body(buildError(ex));
    }
}
```

**Benefits**:
- Decoupled error handling
- Flexible handler chain
- Single responsibility
- Easy to add handlers

**When to Use**:
- Multiple objects can handle request
- Handler not known beforehand
- Want to issue request to multiple objects

***

## Architectural Patterns

### 12. **Layered Architecture (N-Tier)**

**Description**: Organizes system into layers with specific responsibilities.

**Purpose**:
- Separates concerns
- Modular organization
- Independent layer evolution

**Implementation in Project**:
```
┌─────────────────────────────────┐
│   Presentation Layer            │  ← OrderController (REST API)
├─────────────────────────────────┤
│   Service Layer                 │  ← OrderServiceImpl (Business Logic)
├─────────────────────────────────┤
│   Data Access Layer             │  ← OrderRepository (Persistence)
├─────────────────────────────────┤
│   Database Layer                │  ← H2 Database
└─────────────────────────────────┘
```

**Benefits**:
- Clear separation of concerns
- Independent layer changes
- Testable layers
- Maintainable codebase

***

### 13. **MVC Pattern (Model-View-Controller)**

**Description**: Separates application into three interconnected components.

**Purpose**:
- Separates data, presentation, control
- Promotes parallel development
- Multiple views possible

**Implementation in Project**:
```
Model      : Order, OrderItem entities + DTOs
View       : JSON responses (REST API)
Controller : OrderController (HTTP endpoints)
```

**Benefits**:
- Separation of concerns
- Parallel development
- Code reusability
- Easy maintenance

---

### 14. **Service Layer Pattern**

**Description**: Encapsulates business logic separate from presentation and data layers.

**Purpose**:
- Centralizes business logic
- Provides transaction boundaries
- Promotes reusability

**Implementation in Project**:
```java
// Interface
public interface IOrderService {
    OrderResponseDTO createOrder(OrderRequestDTO request);
    OrderResponseDTO getOrderById(Long id);
    // ... business operations
}

// Implementation
@Service
@Transactional
public class OrderServiceImpl implements IOrderService {
    // Business logic implementation
}
```

**Benefits**:
- Encapsulated business logic
- Transaction management
- Easy to test
- Reusable across controllers

***

### 15. **DTO Pattern (Data Transfer Object)**

**Description**: Transfers data between software application subsystems.

**Purpose**:
- Reduces remote calls
- Encapsulates serialization
- Decouples layers

**Implementation in Project**:
```java
// Request DTO
@Data
@Builder
public class OrderRequestDTO {
    @NotBlank @Email
    private String customerEmail;
    @NotEmpty
    private List<OrderItemDTO> items;
}

// Response DTO
@Data
@Builder
public class OrderResponseDTO {
    private Long id;
    private OrderStatus status;
    private String customerEmail;
    private BigDecimal totalAmount;
    private List<OrderItemDTO> items;
    private LocalDateTime createdAt;
}
```

**Benefits**:
- API contract independence
- Security (controlled exposure)
- Versioning support
- Performance optimization

***

## Design Principles

### SOLID Principles

#### 1. **Single Responsibility Principle (SRP)**

**Definition**: A class should have only one reason to change.

**Implementation**:
```java
// OrderController: Only handles HTTP requests
@RestController
public class OrderController { }

// OrderServiceImpl: Only handles business logic
@Service
public class OrderServiceImpl { }

// OrderRepository: Only handles data access
@Repository
public interface OrderRepository { }

// OrderMapper: Only handles DTO-Entity conversion
@Mapper
public interface OrderMapper { }
```

**Benefits**:
- Easier to understand
- Less coupling
- Easier to test
- Better maintainability

---

#### 2. **Open/Closed Principle (OCP)**

**Definition**: Software entities should be open for extension but closed for modification.

**Implementation**:
```java
// Open for extension through interface
public interface IOrderService {
    OrderResponseDTO createOrder(OrderRequestDTO request);
}

// Can add new implementations without modifying existing code
@Service
public class OrderServiceImpl implements IOrderService { }

// Future: Can add AsyncOrderServiceImpl without changing existing code
@Service
public class AsyncOrderServiceImpl implements IOrderService { }
```

**Benefits**:
- Extensible without modification
- Reduces regression bugs
- Promotes polymorphism
- Maintainable codebase

---

#### 3. **Liskov Substitution Principle (LSP)**

**Definition**: Objects should be replaceable with instances of their subtypes without altering correctness.

**Implementation**:
```java
// Any implementation of IOrderService can be used
public interface IOrderService {
    OrderResponseDTO createOrder(OrderRequestDTO request);
}

// Controller works with any IOrderService implementation
@RestController
@RequiredArgsConstructor
public class OrderController {
    private final IOrderService orderService;  // Works with any implementation
    
    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(
            @RequestBody OrderRequestDTO request) {
        return new ResponseEntity<>(orderService.createOrder(request), CREATED);
    }
}
```

**Benefits**:
- Code reliability
- Proper inheritance
- Predictable behavior
- Flexible design

***

#### 4. **Interface Segregation Principle (ISP)**

**Definition**: Clients should not depend on interfaces they don't use.

**Implementation**:
```java
// Single focused interface instead of fat interface
public interface IOrderService {
    OrderResponseDTO createOrder(OrderRequestDTO request);
    OrderResponseDTO getOrderById(Long id);
    Page<OrderResponseDTO> getAllOrders(OrderStatus status, Pageable pageable);
    OrderResponseDTO cancelOrder(Long id);
    OrderResponseDTO updateOrderStatus(Long id, OrderStatus newStatus);
}

// Could be split further if needed:
// public interface IOrderCreationService { }
// public interface IOrderRetrievalService { }
// public interface IOrderManagementService { }
```

**Benefits**:
- Smaller, focused interfaces
- Reduced coupling
- Better maintainability
- Easier to implement

***

#### 5. **Dependency Inversion Principle (DIP)**

**Definition**: High-level modules should not depend on low-level modules. Both should depend on abstractions.

**Implementation**:
```java
// High-level module depends on abstraction
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements IOrderService {
    private final OrderRepository orderRepository;  // Depends on interface
    private final OrderMapper orderMapper;          // Depends on interface
}

// Low-level module implements abstraction
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    // Spring Data JPA provides implementation
}
```

**Benefits**:
- Loose coupling
- Easy to test
- Flexible implementation
- Independent modules

***

### Other Important Principles

#### 6. **DRY (Don't Repeat Yourself)**

**Definition**: Every piece of knowledge should have single representation in system.

**Implementation**:
- MapStruct eliminates manual mapping code
- Global exception handler prevents duplicate error handling
- Service layer reused by multiple controllers
- Repository queries centralized

---

#### 7. **KISS (Keep It Simple, Stupid)**

**Definition**: Systems work best when kept simple.

**Implementation**:
- Simple, clear method names
- Small, focused classes
- Minimal complexity
- Straightforward logic flow

***

#### 8. **YAGNI (You Aren't Gonna Need It)**

**Definition**: Don't add functionality until necessary.

**Implementation**:
- No unused features
- Simple pagination (no complex sorting)
- Basic CRUD operations
- Minimal configuration

***

## Implementation Examples

### Complete Flow Example

```java
// 1. Client Request (HTTP)
POST /api/v1/orders

// 2. Controller (Facade)
@RestController
public class OrderController {
    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(@Valid @RequestBody OrderRequestDTO request) {
        return new ResponseEntity<>(orderService.createOrder(request), CREATED);
    }
}

// 3. Service (Business Logic + Template Method)
@Service
@Transactional
public class OrderServiceImpl implements IOrderService {
    public OrderResponseDTO createOrder(OrderRequestDTO request) {
        validateOrderRequest(request);  // Validation
        Order order = orderMapper.toEntity(request);  // Mapper Pattern
        Order savedOrder = orderRepository.save(order);  // Repository Pattern
        return orderMapper.toResponseDTO(savedOrder);  // Mapper Pattern
    }
}

// 4. Repository (Repository Pattern)
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    // Data access abstraction
}

// 5. Entity (Builder Pattern)
@Entity
@Builder
public class Order {
    // Domain model
}

// 6. DTO (DTO Pattern)
@Data
@Builder
public class OrderResponseDTO {
    // Data transfer
}
```

***

## Summary

This Order Processing System demonstrates professional software engineering through proper application of:

**Design Patterns**: 15 patterns (Singleton, Builder, Factory, Repository, Adapter, Facade, Proxy, Strategy, Template Method, Observer, Chain of Responsibility, Layered Architecture, MVC, Service Layer, DTO)

**Design Principles**: SOLID principles + DRY, KISS, YAGNI

**Benefits**:
- ✅ Maintainable codebase
- ✅ Testable components
- ✅ Scalable architecture
- ✅ Clean, readable code
- ✅ Professional structure

This architecture serves as an excellent foundation for enterprise applications! 🎯