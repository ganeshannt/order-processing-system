package io.ganeshannt.asm.ops.repository;

import io.ganeshannt.asm.ops.entity.Order;
import io.ganeshannt.asm.ops.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Order Repository - Data Access Layer
 *
 * @author Ganeshannt
 * @version 1.2
 * @since 2025-10-23
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {


    /**
     * @param status   Order status filter
     * @param pageable Pagination and sort parameters
     * @return Page of orders
     */
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.status = 'PENDING'")
    List<Order> findAllPendingOrders();

    @Query("""
            SELECT DISTINCT o FROM Order o 
            LEFT JOIN FETCH o.items 
            WHERE o.id = :id
            """)
    Optional<Order> findByIdWithItems(@Param("id") Long id);
}
