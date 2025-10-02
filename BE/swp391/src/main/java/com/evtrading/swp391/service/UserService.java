package com.evtrading.swp391.service;

import com.evtrading.swp391.entity.User;
import com.evtrading.swp391.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

        @Autowired
        private com.evtrading.swp391.repository.RoleRepository roleRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Integer id) {
        return userRepository.findById(id);
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public User updateUser(Integer id, User userDetails) {
        return userRepository.findById(id).map(user -> {
            user.setUsername(userDetails.getUsername());
            user.setEmail(userDetails.getEmail());
            user.setPassword(userDetails.getPassword());
            user.setRole(userDetails.getRole());
            user.setStatus(userDetails.getStatus());
            return userRepository.save(user);
        }).orElse(null);
    }

    public void deleteUser(Integer id) {
        userRepository.deleteById(id);
    }

    public Optional<User> login(String username, String password) {
        return userRepository.findByUsername(username)
                .filter(user -> user.getPassword().equals(password));
    }

        public User register(com.evtrading.swp391.dto.RegisterRequestDTO registerRequestDTO) {
            if (userRepository.findByUsername(registerRequestDTO.getUsername()).isPresent() ||
                userRepository.findByEmail(registerRequestDTO.getEmail()).isPresent()) {
                return null; // Username hoặc email đã tồn tại
            }
            User user = new User();
            user.setUsername(registerRequestDTO.getUsername());
            user.setEmail(registerRequestDTO.getEmail());
            user.setPassword(registerRequestDTO.getPassword());
            user.setStatus(registerRequestDTO.getStatus());
            user.setCreatedAt(new java.util.Date());
            // Lấy role từ roleID
            com.evtrading.swp391.entity.Role role = roleRepository.findById(registerRequestDTO.getRoleID()).orElse(null);
            if (role == null) return null;
            user.setRole(role);
            return userRepository.save(user);
        }
}