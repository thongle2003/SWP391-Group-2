package com.evtrading.swp391.controller;

import com.evtrading.swp391.entity.User;
import com.evtrading.swp391.service.UserService;
import com.evtrading.swp391.dto.LoginRequestDTO;
import com.evtrading.swp391.dto.RegisterRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<User> getUserById(@PathVariable Integer id) {
        Optional<User> user = userService.getUserById(id);
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Tạo mới user
    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.createUser(user);
    }

    // Cập nhật user
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Integer id, @RequestBody User userDetails) {
        User updated = userService.updateUser(id, userDetails);
        if (updated != null)
            return ResponseEntity.ok(updated);
        return ResponseEntity.notFound().build();
    }

    // Xóa user
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
    // ^^
    // ||
    // || api for admin testing

    ///////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////

    // public api for user

    //api login
    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody LoginRequestDTO loginRequestDTO) {
        return userService.login(loginRequestDTO.getUsername(), loginRequestDTO.getPassword())
                .map(ResponseEntity::ok)
                //đăng nhập thành công trả về 200 OK
                .orElseGet(() -> ResponseEntity.status(401).build());
                //đăng nhập thất bại trả về 401 Unauthorized

    }

    //api register
    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody RegisterRequestDTO registerRequestDTO) {
        User user = userService.register(registerRequestDTO);
        if (user != null) {
            return ResponseEntity.status(201).body(user);
            //đăng ký thành công trả về 201 Created
        }
        return ResponseEntity.badRequest().build();
        //đăng ký thất bại trả về 400 Bad Request
    }
}
