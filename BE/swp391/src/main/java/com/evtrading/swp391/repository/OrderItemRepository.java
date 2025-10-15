package com.evtrading.swp391.repository;

import com.evtrading.swp391.entity.OrderItem;
import com.evtrading.swp391.entity.Listing;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
    List<OrderItem> findByListing(Listing listing);
}
