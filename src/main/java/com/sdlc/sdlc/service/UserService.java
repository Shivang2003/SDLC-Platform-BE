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

    @Autowired
    IdGeneratorService idGeneratorService;

    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public boolean createNewUser(User user) {
       try{
           if (userRepository.findByUserName(user.getUserName()) != null) {
               throw new RuntimeException("User already exists");
           }
           if(userRepository.findByEmail(user.getEmail()) != null){
               throw new RuntimeException("Email already in use");
           }
           user.setPassword(passwordEncoder.encode(user.getPassword()));
           user.setRoles(Arrays.asList("USER"));
           user.setUserId(idGeneratorService.generateSequenceId("user","UID"));
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
