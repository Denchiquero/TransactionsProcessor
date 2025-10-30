package com.example.orderservice.repository;

import com.example.orderservice.model.Order;
import com.example.orderservice.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderId(String orderId);
    List<Order> findByCustomerEmail(String customerEmail);
    List<Order> findByStatus(OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.createdAt >= CURRENT_DATE")
    List<Order> findTodayOrders();

    boolean existsByOrderId(String orderId);
}