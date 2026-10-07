package com.sdlc.sdlc.controller;


import com.sdlc.sdlc.dto.AddOrganizationMemberRequest;
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

    @PostMapping("/add-user")
    public ResponseEntity<?> addUserToOrganization(@RequestBody AddOrganizationMemberRequest organizationMemberRequest) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();
            Organization organization = organizationService.checkOrganizationExists(organizationMemberRequest.getOrganizationName());

            if (organization == null) {
                return new ResponseEntity<>(new ErrorResponse("Organization doesn't exist"), HttpStatus.NOT_FOUND);
            }
            OrganizationMember organizationMember = organizationService.findOrganizationMemberByUserName(userName, organization);
            if (organizationMember == null) {
                return new ResponseEntity<>(new ErrorResponse("User is not a member of the organization"), HttpStatus.FORBIDDEN);
            }
            if (!organizationMember.getRole().equals("ADMIN") && !organizationMember.getRole().equals("OWNER")) {
                return new ResponseEntity<>(new ErrorResponse("Only ADMIN or OWNER can add users"), HttpStatus.FORBIDDEN
                );
            }

            User userInDb = userService.findByUserName(organizationMemberRequest.getUserName());
            if (userInDb == null) {
                return new ResponseEntity<>(new ErrorResponse("User doesn't exist"), HttpStatus.NOT_FOUND
                );
            }
            OrganizationMember addedOrganizationMember = organizationService.addMemberToOrganization(organization, userInDb, "VIEWER");
            if(addedOrganizationMember == null){
                return new ResponseEntity<>(new ErrorResponse("User is already a member of the organization"), HttpStatus.CONFLICT);
            }
            return new ResponseEntity<>(addedOrganizationMember,HttpStatus.OK);

        } catch (IllegalStateException e) {
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.CONFLICT);
        }
    }
}
