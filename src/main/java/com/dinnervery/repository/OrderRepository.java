package com.dinnervery.repository;

import com.dinnervery.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByCustomer_Id(Long customerId);
    
    @Query("SELECT o FROM Order o JOIN FETCH o.customer JOIN FETCH o.orderItems oi JOIN FETCH oi.menu WHERE o.id = :orderId")
    Optional<Order> findByIdWithDetails(@Param("orderId") Long orderId);
    
    @Query("SELECT o FROM Order o JOIN FETCH o.customer JOIN FETCH o.orderItems oi JOIN FETCH oi.menu WHERE o.customer.id = :customerId ORDER BY o.createdAt DESC")
    List<Order> findByCustomerIdWithDetails(@Param("customerId") Long customerId);
    
    List<Order> findByDeliveryStatusIn(List<Order.status> statuses);
}
