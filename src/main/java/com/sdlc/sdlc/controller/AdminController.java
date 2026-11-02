package com.sdlc.sdlc.controller;

import com.sdlc.sdlc.dto.CreateOrganizationRequest;
import com.sdlc.sdlc.entity.ErrorResponse;
import com.sdlc.sdlc.entity.Organization;
import com.sdlc.sdlc.entity.User;
import com.sdlc.sdlc.service.OrganizationService;
import com.sdlc.sdlc.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.NoSuchElementException;


@RestController
@RequestMapping("/organization")
@Slf4j
public class AdminController {

    @Autowired
    OrganizationService organizationService;

    @Autowired
    UserService userService;

    @PostMapping("/create")
    public ResponseEntity<?> createOrganization(@RequestBody CreateOrganizationRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();
            User userInDb = userService.findByUserName(userName);
            if (!userInDb.getRoles().contains("ADMIN")) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
            if (organizationService.checkOrganizationNameExists(request.getName()) != null) {
                throw new IllegalStateException("Organization already exists");
            }
            Organization mappedOrganization = organizationService.mapToOrganization(request);
            Organization createdOrganization = organizationService.createNewOrganization(mappedOrganization, userInDb);
            return new ResponseEntity<>(createdOrganization, HttpStatus.CREATED);

        }
        catch (IllegalStateException |
               NoSuchElementException |
               AccessDeniedException e) {
            throw e;
        }catch (Exception e) {
            log.error("Error creating organization: ", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
