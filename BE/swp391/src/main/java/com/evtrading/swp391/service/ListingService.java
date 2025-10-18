package com.evtrading.swp391.service;

import com.evtrading.swp391.dto.*;
import com.evtrading.swp391.entity.*;
import com.evtrading.swp391.mapper.ListingMapper;
import com.evtrading.swp391.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Calendar;

@Service
public class ListingService {

    @Autowired
    private ListingRepository listingRepository;
    
    @Autowired
    private VehicleRepository vehicleRepository;
    
    @Autowired
    private BatteryRepository batteryRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private BrandRepository brandRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ListingImageRepository listingImageRepository;
    
    @Autowired
    private CloudinaryService cloudinaryService;
    
    @Autowired
    private ListingMapper listingMapper;

    /**
     * Tạo một bài đăng mới
     */
    @Transactional
    public ListingResponseDTO createListing(ListingRequestDTO dto, List<MultipartFile> images, String username) {
        // 1. Lấy thông tin người dùng
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // 2. Lấy category và brand
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
                
        Brand brand = brandRepository.findById(dto.getBrandId())
                .orElseThrow(() -> new RuntimeException("Brand not found"));

        // 3. Tạo và lưu Vehicle hoặc Battery tùy thuộc vào loại sản phẩm
        Vehicle vehicle = null;
        Battery battery = null;
        
        if (dto.getVehicle() != null) {
            vehicle = createVehicleFromDTO(dto.getVehicle(), category, brand);
        } else if (dto.getBattery() != null) {
            battery = createBatteryFromDTO(dto.getBattery(), category, brand);
        } else {
            throw new RuntimeException("Either vehicle or battery information must be provided");
        }

        // 4. Tạo Listing
        Listing listing = new Listing();
        listing.setUser(user);
        listing.setCategory(category);
        listing.setBrand(brand);
        listing.setTitle(dto.getTitle());
        listing.setDescription(dto.getDescription());
        listing.setPrice(dto.getPrice());
        listing.setStatus("PENDING"); // Trạng thái mặc định khi tạo mới
        listing.setCreatedAt(new Date());
        
        // Nếu là xe
        if (vehicle != null) {
            listing.setVehicle(vehicle);
        } 
        // Nếu là pin
        else if (battery != null) {
            listing.setBattery(battery);
        }

        // 5. Lưu Listing
        Listing savedListing = listingRepository.save(listing);

        List<String> imageUrls = images != null ? cloudinaryService.uploadImages(images) : dto.getImageURLs();
        List<ListingImage> listingImages = saveListingImages(savedListing, imageUrls, dto.getPrimaryImageIndex());

        return convertToListingResponseDTO(savedListing, listingImages);
    }

    /**
     * Lấy chi tiết một bài đăng
     */
    public ListingResponseDTO getListingById(Integer id) {
        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Listing not found with id: " + id));
                
        List<ListingImage> images = listingImageRepository.findByListingListingID(id);
        
        return convertToListingResponseDTO(listing, images);
    }

    /**
     * Cập nhật một bài đăng
     */
    @Transactional
    public ListingResponseDTO updateListing(Integer listingId, ListingRequestDTO dto, String username) {
        // 1. Tìm listing cần cập nhật và kiểm tra quyền
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new RuntimeException("Listing not found with id: " + listingId));
                
        // Kiểm tra quyền
        if (!listing.getUser().getUsername().equals(username)) {
            throw new RuntimeException("You don't have permission to update this listing");
        }
        
        // 2. Cập nhật thông tin cơ bản
        listing.setTitle(dto.getTitle());
        listing.setDescription(dto.getDescription());
        listing.setPrice(dto.getPrice());
        
        // 3. Cập nhật category và brand nếu cần
        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            listing.setCategory(category);
        }
        
        if (dto.getBrandId() != null) {
            Brand brand = brandRepository.findById(dto.getBrandId())
                    .orElseThrow(() -> new RuntimeException("Brand not found"));
            listing.setBrand(brand);
        }
        
        // 4. Cập nhật thông tin Vehicle hoặc Battery
        if (dto.getVehicle() != null && listing.getVehicle() != null) {
            updateVehicle(listing.getVehicle(), dto.getVehicle());
        } else if (dto.getBattery() != null && listing.getBattery() != null) {
            updateBattery(listing.getBattery(), dto.getBattery());
        }
        
        // 5. Lưu Listing
        Listing updatedListing = listingRepository.save(listing);
        
        // 6. Cập nhật hình ảnh nếu cần
        List<ListingImage> images = listing.getListingID() != null && dto.getImageURLs() != null ? 
                updateListingImages(updatedListing, dto.getImageURLs(), dto.getPrimaryImageIndex()) : 
                listingImageRepository.findByListingListingID(listingId);
        
        // 7. Trả về response
        return convertToListingResponseDTO(updatedListing, images);
    }

    /**
     * Xóa một bài đăng
     */
    @Transactional
    public void deleteListing(Integer id, String username) {
        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Listing not found"));
                
        // Kiểm tra quyền
        if (!listing.getUser().getUsername().equals(username)) {
            throw new RuntimeException("You don't have permission to delete this listing");
        }
        
        // Xóa hình ảnh trước
        listingImageRepository.deleteByListingListingID(id);
        
        // Xóa listing
        listingRepository.delete(listing);
    }

    /**
     * Lấy danh sách bài đăng theo các tiêu chí
     * Mặc định chỉ lấy các bài đăng đã được approve (ACTIVE)
     */
    public Page<ListingResponseDTO> getListings(String status, Integer userId, 
                                         Integer categoryId, Integer brandId, 
                                         Pageable pageable, boolean isModerator) {
        Page<Listing> listingsPage;
        
        // Nếu là admin/moderator xem bài đăng cụ thể theo status
        if (status != null && isModerator) {
            listingsPage = listingRepository.findByStatus(status, pageable);
        } 
        // Nếu người dùng muốn xem bài đăng của họ
        else if (userId != null) {
            if (isModerator) {
                // Moderator có thể xem tất cả bài đăng của user
                listingsPage = listingRepository.findByUserUserID(userId, pageable);
            } else {
                // User thường chỉ xem được bài đăng ACTIVE của người khác hoặc tất cả bài đăng của chính họ
                listingsPage = listingRepository.findByUserUserID(userId, pageable);
            }
        }
        // Lọc theo danh mục 
        else if (categoryId != null) {
            if (isModerator && status != null) {
                listingsPage = listingRepository.findByCategoryCategoryIDAndStatus(categoryId, status, pageable);
            } else {
                listingsPage = listingRepository.findByCategoryCategoryIDAndStatus(categoryId, "ACTIVE", pageable);
            }
        }
        // Lọc theo thương hiệu
        else if (brandId != null) {
            if (isModerator && status != null) {
                listingsPage = listingRepository.findByBrandBrandIDAndStatus(brandId, status, pageable);
            } else {
                listingsPage = listingRepository.findByBrandBrandIDAndStatus(brandId, "ACTIVE", pageable);
            }
        }
        // Mặc định chỉ lấy các bài đăng ACTIVE
        else {
            if (isModerator && status != null) {
                listingsPage = listingRepository.findByStatus(status, pageable);
            } else {
                listingsPage = listingRepository.findByStatus("ACTIVE", pageable);
            }
        }
        
        return listingsPage.map(listing -> {
            List<ListingImage> images = listingImageRepository.findByListingListingID(listing.getListingID());
            return convertToListingResponseDTO(listing, images);
        });
    }

    /**
     * Phê duyệt bài đăng (chỉ Moderator)
     */
    @Transactional
    public ListingResponseDTO approveListing(Integer id) {
        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Listing not found with id: " + id));
                
        // Kiểm tra xem bài đăng có phải đang ở trạng thái PENDING không
        if (!"PENDING".equals(listing.getStatus())) {
            throw new RuntimeException("Only pending listings can be approved");
        }
        
        // Cập nhật trạng thái và các ngày
        listing.setStatus("ACTIVE");
        listing.setStartDate(new Date());
        
        // Thiết lập ngày hết hạn (ví dụ 30 ngày sau)
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, 30);
        listing.setExpiryDate(calendar.getTime());
        
        Listing savedListing = listingRepository.save(listing);
        
        List<ListingImage> images = listingImageRepository.findByListingListingID(id);
        
        return convertToListingResponseDTO(savedListing, images);
    }

    /**
     * Từ chối bài đăng (chỉ Moderator)
     */
    @Transactional
    public ListingResponseDTO rejectListing(Integer id, String reason) {
        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Listing not found with id: " + id));
                
        // Kiểm tra xem bài đăng có phải đang ở trạng thái PENDING không
        if (!"PENDING".equals(listing.getStatus())) {
            throw new RuntimeException("Only pending listings can be rejected");
        }
        
        // Cập nhật trạng thái
        listing.setStatus("REJECTED");
        // Lưu lý do từ chối nếu có
        if (reason != null && !reason.isEmpty()) {
            // Cần thêm trường rejectionReason vào entity Listing
            // Hoặc có thể lưu vào trường khác đã có
            listing.setRejectionReason(reason);
        }
        
        Listing savedListing = listingRepository.save(listing);
        
        List<ListingImage> images = listingImageRepository.findByListingListingID(id);
        
        return convertToListingResponseDTO(savedListing, images);
    }

    /**
     * Lấy danh sách các bài đăng đang chờ phê duyệt (PENDING)
     * Chỉ dành cho moderator
     */
    public Page<ListingResponseDTO> getPendingListings(Pageable pageable) {
        Page<Listing> pendingListingsPage = listingRepository.findByStatus("PENDING", pageable);
        
        return pendingListingsPage.map(listing -> {
            List<ListingImage> images = listingImageRepository.findByListingListingID(listing.getListingID());
            return convertToListingResponseDTO(listing, images);
        });
    }

    // Các phương thức helper bên dưới

    private Vehicle createVehicleFromDTO(VehicleDTO dto, Category category, Brand brand) {
        Vehicle vehicle = new Vehicle();
        vehicle.setCategory(category);
        vehicle.setBrand(brand);
        vehicle.setModel(dto.getModel());
        vehicle.setColor(dto.getColor());
        vehicle.setYear(dto.getYear());
        vehicle.setPrice(dto.getPrice());
        vehicle.setCondition(dto.getCondition());
        
        return vehicleRepository.save(vehicle);
    }
    
    private Battery createBatteryFromDTO(BatteryDTO dto, Category category, Brand brand) {
        Battery battery = new Battery();
        battery.setCategory(category);
        battery.setBrand(brand);
        battery.setCapacity(dto.getCapacity());
        battery.setVoltage(dto.getVoltage());
        battery.setCycleCount(dto.getCycleCount());
        battery.setPrice(dto.getPrice());
        battery.setCondition(dto.getCondition());
        
        return batteryRepository.save(battery);
    }
    
    private void updateVehicle(Vehicle vehicle, VehicleDTO dto) {
        if (dto.getModel() != null) vehicle.setModel(dto.getModel());
        if (dto.getColor() != null) vehicle.setColor(dto.getColor());
        if (dto.getYear() != null) vehicle.setYear(dto.getYear());
        if (dto.getPrice() != null) vehicle.setPrice(dto.getPrice());
        if (dto.getCondition() != null) vehicle.setCondition(dto.getCondition());
        
        vehicleRepository.save(vehicle);
    }
    
    private void updateBattery(Battery battery, BatteryDTO dto) {
        if (dto.getCapacity() != null) battery.setCapacity(dto.getCapacity());
        if (dto.getVoltage() != null) battery.setVoltage(dto.getVoltage());
        if (dto.getCycleCount() != null) battery.setCycleCount(dto.getCycleCount());
        if (dto.getPrice() != null) battery.setPrice(dto.getPrice());
        if (dto.getCondition() != null) battery.setCondition(dto.getCondition());
        
        batteryRepository.save(battery);
    }
    
    private List<ListingImage> saveListingImages(Listing listing, List<String> imageURLs, Integer primaryIndex) {
        if (imageURLs == null || imageURLs.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<ListingImage> images = new ArrayList<>();
        
        for (int i = 0; i < imageURLs.size(); i++) {
            ListingImage image = new ListingImage();
            image.setListing(listing);
            image.setImageURL(imageURLs.get(i));
            image.setIsPrimary(primaryIndex != null && primaryIndex == i);
            images.add(listingImageRepository.save(image));
        }
        
        return images;
    }
    
    private List<ListingImage> updateListingImages(Listing listing, List<String> imageURLs, Integer primaryIndex) {
        // Xóa hình ảnh cũ
        listingImageRepository.deleteByListingListingID(listing.getListingID());
        
        // Thêm hình ảnh mới
        return saveListingImages(listing, imageURLs, primaryIndex);
    }
    
    private ListingResponseDTO convertToListingResponseDTO(Listing listing, List<ListingImage> images) {
        return listingMapper.toDto(listing, images);
    }
}
