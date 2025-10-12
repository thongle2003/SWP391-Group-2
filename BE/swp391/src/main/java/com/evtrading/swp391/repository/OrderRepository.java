package com.evtrading.swp391.repository;

import com.evtrading.swp391.entity.Order;
import com.evtrading.swp391.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findByBuyer(User buyer);
}
