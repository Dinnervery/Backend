package com.dinnervery.backend.repository;

import com.dinnervery.backend.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrder_Id(Long orderId);
    
    List<OrderItem> findByMenu_Id(Long menuId);
}


