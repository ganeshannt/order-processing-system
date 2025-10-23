package io.ganeshannt.asm.ops.repository;

import io.ganeshannt.asm.ops.entity.Order;
import io.ganeshannt.asm.ops.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Order Repository - Data Access Layer
 *
 * Why interface instead of class?
 * - Spring Data JPA automatically implements this at runtime
 * - No need to write boilerplate CRUD code
 * - Type-safe query methods
 *
 * Query Method Naming Convention:
 * - findBy[Field][Operator]: Spring generates query automatically
 * - Example: findByStatus → SELECT * FROM orders WHERE status = ?
 *
 * Why @Query for complex queries?
 * - Better performance: Can optimize with JOIN FETCH
 * - Explicit control: Clear intent
 * - Prevents N+1 problem: Fetch associations in single query
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Find orders by status
     *
     * Spring Data JPA Method Name Query
     * - No @Query needed, Spring generates SQL from method name
     *
     * Generated SQL:
     * SELECT * FROM orders WHERE status = :status
     */
    List<Order> findByStatus(OrderStatus status);

    /**
     * Find all PENDING orders
     *
     * Why custom @Query?
     * - More explicit than method name query
     * - Can add complex conditions later
     * - Better readability for complex queries
     */
    @Query("SELECT o FROM Order o WHERE o.status = 'PENDING'")
    List<Order> findAllPendingOrders();

    /**
     * Find orders by status WITH items (solving N+1 problem)
     *
     * Why JOIN FETCH?
     * - Fetches Order and OrderItems in SINGLE query
     * - Without it: 1 query for orders + N queries for items (N+1 problem)
     * - With it: 1 query total
     *
     * Performance Impact:
     * - Without JOIN FETCH: 1 + N queries (slow)
     * - With JOIN FETCH: 1 query (fast)
     *
     * LEFT JOIN vs INNER JOIN?
     * - LEFT JOIN: Returns orders even if they have no items
     * - INNER JOIN: Returns only orders that have items
     * - Use LEFT JOIN for optional relationships
     */
    @Query("""
        SELECT DISTINCT o FROM Order o 
        LEFT JOIN FETCH o.items 
        WHERE o.status = :status
        """)
    List<Order> findByStatusWithItems(@Param("status") OrderStatus status);

    /**
     * Find order by ID with items eagerly loaded
     *
     * Why this method?
     * - When getting single order, we almost always need items
     * - Avoids lazy loading exception
     * - Better performance than separate queries
     *
     * DISTINCT keyword:
     * - Prevents duplicate Order objects when multiple items exist
     * - JPA handles this correctly
     */
    @Query("""
        SELECT DISTINCT o FROM Order o 
        LEFT JOIN FETCH o.items 
        WHERE o.id = :id
        """)
    Optional<Order> findByIdWithItems(@Param("id") Long id);

}
