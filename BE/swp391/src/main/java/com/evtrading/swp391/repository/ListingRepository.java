package com.evtrading.swp391.repository;

import com.evtrading.swp391.entity.Listing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ListingRepository extends JpaRepository<Listing, Integer> {
    List<Listing> findByUserUserID(Integer userID);
}
