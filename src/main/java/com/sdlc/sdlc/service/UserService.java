package com.sdlc.sdlc.service;

import com.sdlc.sdlc.entity.User;
import com.sdlc.sdlc.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@Slf4j
public class UserService {

    @Autowired
    UserRepository userRepository;

    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public boolean createNewUser(User user) {
       try{
           user.setPassword(passwordEncoder.encode(user.getPassword()));
           user.setRoles(Arrays.asList("USER"));
           userRepository.save(user);
           log.info("New user created: {}", user.getUserName());
           return true;
       }
       catch (Exception e){
           throw new RuntimeException("Error creating user: " + e.getMessage());
       }
    }

    public User findByUserName(String userName) {
        return userRepository.findByUserName(userName);
    }
}
