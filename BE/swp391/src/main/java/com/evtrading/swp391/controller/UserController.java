package com.evtrading.swp391.controller;

import com.evtrading.swp391.entity.User;
import com.evtrading.swp391.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    // Lấy danh sách tất cả user
    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    // Lấy thông tin user theo id
    @GetMapping("/{id}")
    public Optional<User> getUserById(@PathVariable Integer id) {
        return userService.getUserById(id);
    }

    // Tạo mới user
    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.createUser(user);
    }

    // Cập nhật user
    @PutMapping("/{id}")
    public User updateUser(@PathVariable Integer id, @RequestBody User userDetails) {
        return userService.updateUser(id, userDetails);
    }

    // Xóa user
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
    }
}
