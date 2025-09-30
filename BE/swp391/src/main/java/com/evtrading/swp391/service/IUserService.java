package com.evtrading.swp391.service;

import com.evtrading.swp391.entity.User;

import java.util.List;
import java.util.Optional;

public interface IUserService {
    List<User> getAllUsers();
    Optional<User> getUserById(Integer id);
    User createUser(User user);
    User updateUser(Integer id, User userDetails);
    void deleteUser(Integer id);
}
