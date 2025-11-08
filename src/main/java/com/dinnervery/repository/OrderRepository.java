package com.dinnervery.repository;

import com.dinnervery.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByCustomer_Id(Long customerId);
    
    @Query("SELECT DISTINCT o FROM Order o " +
           "JOIN FETCH o.customer " +
           "JOIN FETCH o.orderItems oi " +
           "JOIN FETCH oi.menu " +
           "JOIN FETCH oi.style " +
           "WHERE o.id = :orderId")
    Optional<Order> findByIdWithDetails(@Param("orderId") Long orderId);
    
    @Query("SELECT DISTINCT o FROM Order o " +
           "JOIN FETCH o.customer " +
           "JOIN FETCH o.orderItems oi " +
           "JOIN FETCH oi.menu " +
           "JOIN FETCH oi.style " +
           "WHERE o.customer.id = :customerId ORDER BY o.createdAt DESC")
    List<Order> findByCustomerIdWithDetails(@Param("customerId") Long customerId);
    
    List<Order> findByDeliveryStatusIn(List<Order.Status> statuses);
    
    @Query("SELECT DISTINCT o FROM Order o " +
           "JOIN FETCH o.customer " +
           "JOIN FETCH o.orderItems oi " +
           "JOIN FETCH oi.menu " +
           "JOIN FETCH oi.style " +
           "WHERE o.deliveryStatus IN :statuses")
    List<Order> findByDeliveryStatusInWithDetails(@Param("statuses") List<Order.Status> statuses);
}
