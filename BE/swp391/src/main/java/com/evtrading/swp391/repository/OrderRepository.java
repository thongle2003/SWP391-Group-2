package com.evtrading.swp391.repository;

import com.evtrading.swp391.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.evtrading.swp391.entity.User;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findByBuyerOrderByCreatedAtDesc(User buyer);

}