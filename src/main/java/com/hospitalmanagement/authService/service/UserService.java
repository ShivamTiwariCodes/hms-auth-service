package com.hospitalmanagement.authService.service;

import com.hospitalmanagement.authService.model.user.User;
import com.hospitalmanagement.authService.repository.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public String getRoleByUsername(String username) {
        return findByUsername(username).getRole();
    }
}
