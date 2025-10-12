package com.evtrading.swp391.controller;

import com.evtrading.swp391.dto.ListingRequestDTO;
import com.evtrading.swp391.dto.ListingResponseDTO;
import com.evtrading.swp391.service.ListingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.Calendar;

@RestController
@RequestMapping("/api/listings")
@Tag(name = "Listings", description = "API để quản lý bài đăng sản phẩm")
public class ListingController {

    @Autowired
    private ListingService listingService;

    @Operation(summary = "Tạo bài đăng mới", description = "Đăng bài mới cho xe hoặc pin")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ResponseEntity<ListingResponseDTO> createListing(
            @RequestBody ListingRequestDTO listingRequest,
            Authentication authentication) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            String username = authentication.getName();
            ListingResponseDTO createdListing = listingService.createListing(listingRequest, username);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdListing);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // Sửa phương thức getListings để rõ ràng hơn trong việc lọc bài đăng
    @Operation(summary = "Lấy danh sách bài đăng", description = "Lấy danh sách bài đăng theo các tiêu chí")
    @GetMapping
    public ResponseEntity<Page<ListingResponseDTO>> getListings(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Integer brandId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Authentication authentication) {
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        boolean isModerator = false;
        
        // Kiểm tra role của người dùng
        if (authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MODERATOR") || a.getAuthority().equals("ROLE_ADMIN"))) {
            isModerator = true;
        }
        
        Page<ListingResponseDTO> listings = listingService.getListings(status, userId, categoryId, brandId, pageable, isModerator);
        return ResponseEntity.ok(listings);
    }

    @Operation(summary = "Lấy chi tiết bài đăng", description = "Lấy thông tin chi tiết của một bài đăng")
    @GetMapping("/{id}")
    public ResponseEntity<ListingResponseDTO> getListingById(@PathVariable Integer id) {
        try {
            ListingResponseDTO listing = listingService.getListingById(id);
            return ResponseEntity.ok(listing);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Cập nhật bài đăng", description = "Cập nhật thông tin của một bài đăng")
    @PutMapping("/{id}")
    public ResponseEntity<ListingResponseDTO> updateListing(
            @PathVariable Integer id,
            @RequestBody ListingRequestDTO listingRequest,
            Authentication authentication) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            String username = authentication.getName();
            ListingResponseDTO updatedListing = listingService.updateListing(id, listingRequest, username);
            return ResponseEntity.ok(updatedListing);
        } catch (Exception e) {
            if (e.getMessage().contains("permission")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @Operation(summary = "Xóa bài đăng", description = "Xóa một bài đăng theo ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteListing(
            @PathVariable Integer id,
            Authentication authentication) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            String username = authentication.getName();
            listingService.deleteListing(id, username);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            if (e.getMessage().contains("permission")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // Thêm API endpoint mới cho approve/reject
    @Operation(summary = "Phê duyệt bài đăng", description = "Moderator phê duyệt bài đăng")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<ListingResponseDTO> approveListing(@PathVariable Integer id) {
        try {
            ListingResponseDTO approvedListing = listingService.approveListing(id);
            return ResponseEntity.ok(approvedListing);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null);
        }
    }

    @Operation(summary = "Từ chối bài đăng", description = "Moderator từ chối bài đăng")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<ListingResponseDTO> rejectListing(
            @PathVariable Integer id,
            @RequestParam(required = false) String reason) {
        try {
            ListingResponseDTO rejectedListing = listingService.rejectListing(id, reason);
            return ResponseEntity.ok(rejectedListing);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null);
        }
    }

    @Operation(summary = "Lấy danh sách bài đăng chờ phê duyệt", description = "Dành cho moderator để xem các bài đăng đang chờ phê duyệt")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/pending")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<Page<ListingResponseDTO>> getPendingListings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<ListingResponseDTO> pendingListings = listingService.getPendingListings(pageable);
        return ResponseEntity.ok(pendingListings);
    }
}
