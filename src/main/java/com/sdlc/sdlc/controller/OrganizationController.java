package com.sdlc.sdlc.controller;


import com.sdlc.sdlc.entity.ErrorResponse;
import com.sdlc.sdlc.entity.Organization;
import com.sdlc.sdlc.entity.OrganizationMember;
import com.sdlc.sdlc.entity.User;
import com.sdlc.sdlc.service.OrganizationService;
import com.sdlc.sdlc.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/organization")
@Slf4j
public class OrganizationController {

    @Autowired
    OrganizationService organizationService;

    @Autowired
    UserService userService;

    @PostMapping("/{organizationName}/add-user")
    public ResponseEntity<?> addUserToOrganization(@RequestParam String organizationName, @RequestBody User user) {
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();
            Organization organization = organizationService.checkOrganizationExists(organizationName);
            if(organization == null){
                return new ResponseEntity<>(new ErrorResponse("Organization doesn't exist"),HttpStatus.FORBIDDEN);
            }
            OrganizationMember organizationMember = organizationService.findOrganizationMemberByUserName(userName);
            if (organizationMember.getRole().equals("ADMIN")) {
                User userInDb = userService.findByUserName(user.getUserName()); //need to create DTO
                organizationService.addMemberToOrganization(userInDb, "VIEWER");
                return new ResponseEntity<>(HttpStatus.OK);
            }
            else{
                return new ResponseEntity<>(new ErrorResponse("only ADMIN can add users"),HttpStatus.FORBIDDEN);
            }
        }
        catch (Exception e){
            log.error("Error adding user to organization: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error adding user to organization");
        }
    }
}
