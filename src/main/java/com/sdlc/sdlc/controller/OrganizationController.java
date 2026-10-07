package com.sdlc.sdlc.controller;


import com.sdlc.sdlc.dto.AddOrganizationMemberRequest;
import com.sdlc.sdlc.entity.*;
import com.sdlc.sdlc.service.OrganizationService;
import com.sdlc.sdlc.service.ProjectService;
import com.sdlc.sdlc.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/organization")
@Slf4j
public class OrganizationController {

    @Autowired
    OrganizationService organizationService;

    @Autowired
    UserService userService;

    @Autowired
    ProjectService projectService;

    @PostMapping("/{organizationId}/add-member")
    public ResponseEntity<?> addUserToOrganization(@RequestParam String organizationId,@RequestBody AddOrganizationMemberRequest organizationMemberRequest) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();
            Organization organization = organizationService.checkOrganizationNameExists(organizationMemberRequest.getOrganizationName());

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

//    @GetMapping("/{organizationId}/projects")
//    public ResponseEntity<?> getProjectsOfOrganization(@PathVariable String organizationId) {
//        try{
//
//        }
//    }
    @PostMapping("/{organizationId}/create-project")
    public ResponseEntity<?> createProjectInOrganization(@PathVariable String organizationId, @RequestBody Project project) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();
            Organization organization = organizationService.checkOrganizationIdExists(organizationId);
            if(organization == null) {
                return new ResponseEntity<>(new ErrorResponse("Organization doesn't exist"), HttpStatus.NOT_FOUND);
            }

            OrganizationMember organizationMember = organizationService.findOrganizationMemberByUserName(userName, organization);
            if (organizationMember == null) {
                return new ResponseEntity<>(new ErrorResponse("User is not a member of the organization"), HttpStatus.FORBIDDEN);
            }
            if (!organizationMember.getRole().equals("ADMIN") && !organizationMember.getRole().equals("OWNER")) {
                return new ResponseEntity<>(new ErrorResponse("Only ADMIN or OWNER can create projects"), HttpStatus.FORBIDDEN
                );
            }

            Project existingProject = projectService.checkProjectNameExists(organization,project.getName());
            if(existingProject != null) {
                return new ResponseEntity<>(new ErrorResponse("Project with the same name already exists in the organization"), HttpStatus.CONFLICT);
            }
            Project createdProject = projectService.createNewProject(organization, project, organizationMember);
            if(createdProject == null) {
                return new ResponseEntity<>(new ErrorResponse("Failed to create project"), HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return new ResponseEntity<>(createdProject, HttpStatus.CREATED);
        }
        catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.CONFLICT);
            }

    }

}
