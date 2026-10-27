
package com.sdlc.sdlc.controller;

import com.sdlc.sdlc.entity.Organization;
import com.sdlc.sdlc.entity.OrganizationMember;
import com.sdlc.sdlc.entity.User;
import com.sdlc.sdlc.repository.OrganizationMemberRepository;
import com.sdlc.sdlc.repository.OrganizationRepository;
import com.sdlc.sdlc.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    OrganizationMemberRepository organizationMemberRepository;

    @Autowired
    UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(Map.of("userName", authentication.getName()), HttpStatus.OK);
    }

    @GetMapping("/organizations")
    public ResponseEntity<?> getOrganizations(){
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();
            User user = userRepository.findByUserName(userName);
            List<OrganizationMember> member = organizationMemberRepository.findByUser(user);
            List<Organization> organizations = member.stream().map(OrganizationMember::getOrganization).toList();
            return new ResponseEntity<>(organizations,HttpStatus.OK);
        }
        catch (Exception e){
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

    }

    @GetMapping("/allUsers")
    public ResponseEntity<?> getAllUsers() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();
            List<Map<String, String>> users = userRepository.findAll()
                    .stream()
                    .filter(user -> user.getUserId() != null)
                    .map(user -> Map.of(
                            "userId", user.getUserId(),
                            "userName", user.getUserName()
                    ))
                    .toList();

            return new ResponseEntity<>(users, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }

}