package com.sdlc.sdlc.controller;

import com.sdlc.sdlc.entity.LoginResponse;
import com.sdlc.sdlc.entity.User;
import com.sdlc.sdlc.service.UserDetailsServiceImpl;
import com.sdlc.sdlc.service.UserService;
import com.sdlc.sdlc.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/public")
@Slf4j
public class PublicController {

    @Autowired
    UserService userService;

    @Autowired
    public AuthenticationManager authenticationManager;

    @Autowired
    public UserDetailsServiceImpl userDetailsServiceImpl;

    @Autowired
    public JwtUtils jwtUtils;

    @PostMapping("/sign-up")
    public ResponseEntity<?> createUser(@RequestBody User user) {

        boolean status = userService.createNewUser(user);
        if(status){
            return new ResponseEntity<>(user, HttpStatus.CREATED);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/login")
    public ResponseEntity<?>loginUser(@RequestBody User user){
        try{
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getUserName(), user.getPassword()));
            UserDetails userDetails = userDetailsServiceImpl.loadUserByUsername(user.getUserName());
            String token = jwtUtils.generateToken(userDetails.getUsername());
            return new ResponseEntity<>(new LoginResponse(token), HttpStatus.OK);
        } catch (Exception e) {
            log.info("Error during login: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
