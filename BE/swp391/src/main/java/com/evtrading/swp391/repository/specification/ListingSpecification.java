package com.evtrading.swp391.repository.specification;

import org.springframework.data.jpa.domain.Specification;

import com.evtrading.swp391.entity.Listing;

public class ListingSpecification {

    /**
     * Tạo điều kiện lọc theo status.
     * Trả về một điều kiện luôn đúng nếu status là null.
     */
    public static Specification<Listing> hasStatus(String status) {
        return (root, query, criteriaBuilder) ->
                status == null ? criteriaBuilder.conjunction() : criteriaBuilder.equal(root.get("status"), status);
    }

    /**
     * Tạo điều kiện lọc theo categoryId.
     * root.get("category").get("id") sẽ tạo ra join đến bảng Category và so sánh ID.
     */
    public static Specification<Listing> hasCategory(Integer categoryId) {
        return (root, query, criteriaBuilder) ->
                categoryId == null ? criteriaBuilder.conjunction() : criteriaBuilder.equal(root.get("category").get("id"), categoryId);
    }

    /**
     * Tạo điều kiện lọc theo brandId.
     */
    public static Specification<Listing> hasBrand(Integer brandId) {
        return (root, query, criteriaBuilder) ->
                brandId == null ? criteriaBuilder.conjunction() : criteriaBuilder.equal(root.get("brand").get("id"), brandId);
    }

    /**
     * Tạo điều kiện lọc theo userId.
     */
    public static Specification<Listing> hasUser(Integer userId) {
        return (root, query, criteriaBuilder) ->
                userId == null ? criteriaBuilder.conjunction() : criteriaBuilder.equal(root.get("user").get("id"), userId);
    }
}
