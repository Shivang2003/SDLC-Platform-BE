package com.sdlc.sdlc.controller;

import com.sdlc.sdlc.entity.ErrorResponse;
import com.sdlc.sdlc.entity.Organization;
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


@Controller
@RequestMapping("/organization")
@Slf4j
public class Admin {

    @Autowired
    OrganizationService organizationService;

    @Autowired
    UserService userService;

    @PostMapping("/create")
    public ResponseEntity<?> createOrganization(@RequestBody Organization organization) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();
            User userInDb = userService.findByUserName(userName);
            if (!userInDb.getRoles().contains("ADMIN")) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
            if (organizationService.checkOrganizationNameExists(organization.getName()) != null) {
                return new ResponseEntity<>(new ErrorResponse("Organization already exists"), HttpStatus.CONFLICT);
            }
            Organization createdOrganization = organizationService.createNewOrganization(organization, userInDb);
            return new ResponseEntity<>(createdOrganization, HttpStatus.CREATED);

        } catch (Exception e) {
            log.error("Error creating organization: ", e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
