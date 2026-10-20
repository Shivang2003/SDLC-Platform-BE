package com.sdlc.sdlc.controller;

import com.sdlc.sdlc.dto.AddProjectMemberRequest;
import com.sdlc.sdlc.entity.*;
import com.sdlc.sdlc.enums.ProjectRoles;
import com.sdlc.sdlc.repository.OrganizationMemberRepository;
import com.sdlc.sdlc.repository.ProjectMemberRepository;
import com.sdlc.sdlc.repository.ProjectRepository;
import com.sdlc.sdlc.service.FeatureService;
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

import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/project")
@Slf4j
public class ProjectController {

    @Autowired
    ProjectService projectService;

    @Autowired
    FeatureService featureService;

    @Autowired
    UserService userService;

    @Autowired
    OrganizationMemberRepository organizationMemberRepository;

    @Autowired
    ProjectMemberRepository projectMemberRepository;

    @Autowired
    ProjectRepository projectRepository;

    @PostMapping("/{projectId}/create-feature")
    public ResponseEntity<?> createFeature(@PathVariable String projectId, @RequestBody Feature feature) {
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();
            ProjectMember projectMember = featureService.validateFeatureEditAccess(projectId, userName, Set.of(ProjectRoles.VIEWER));
            if(projectMember == null) {
                return new ResponseEntity<>(new ErrorResponse("User is not a member of the project"), HttpStatus.FORBIDDEN);
            }
            Project projectInDb = projectService.findProjectById(projectId);
            if (projectInDb == null) {
                return new ResponseEntity<>(new ErrorResponse("Project doesn't exist"), HttpStatus.NOT_FOUND);
            }
            Feature checkFeature = featureService.checkIfFeatureNameExists(feature.getName(), projectInDb);
            if(checkFeature != null){
                return new ResponseEntity<>(new ErrorResponse("Feature already exists"), HttpStatus.CONFLICT);
            }
            featureService.createNewFeature(feature, projectInDb, projectMember);

            return new ResponseEntity<>(feature, HttpStatus.OK);
        }
        catch (Exception e) {
            log.error("Error creating feature: ", e);
            return ResponseEntity.badRequest().body("Error creating feature");
        }
    }

    @PostMapping("/{projectId}/add-member")
    public ResponseEntity<?> addUserToProject(@PathVariable String projectId, @RequestBody AddProjectMemberRequest userDetails) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUserName = authentication.getName();
            Project project = projectRepository.findByProjectId(projectId);
            if (project == null) {
                return new ResponseEntity<>(new ErrorResponse("Project doesn't exist"), HttpStatus.NOT_FOUND);
            }

            User currentUser = userService.findByUserName(currentUserName);
            OrganizationMember currentOrgMember = organizationMemberRepository.findByUserAndOrganization(currentUser, project.getOrganization());

            if (currentOrgMember == null) {
                return new ResponseEntity<>(new ErrorResponse("You are not a member of this organization"), HttpStatus.FORBIDDEN);
            }
            ProjectMember currentProjectMember = projectMemberRepository.findByProjectAndOrganizationMember(project, currentOrgMember).orElse(null);

            if (currentProjectMember == null || !"ADMIN".equals(currentProjectMember.getRole())) {
                return new ResponseEntity<>(new ErrorResponse("Only project admins can add members"), HttpStatus.FORBIDDEN);
            }
            User newUser = userService.findByUserName(userDetails.getUserName());

            if (newUser == null) {
                return new ResponseEntity<>(new ErrorResponse("User doesn't exist"), HttpStatus.NOT_FOUND);
            }
            OrganizationMember newOrgMember = organizationMemberRepository.findByUserAndOrganization(newUser, project.getOrganization());

            if (newOrgMember == null) {
                return new ResponseEntity<>(new ErrorResponse("User is not a member of this organization"), HttpStatus.BAD_REQUEST);
            }
            String role = userDetails.getRole();

            if (!List.of("ADMIN", "MEMBER", "VIEWER").contains(role)) {
                return new ResponseEntity<>(new ErrorResponse("Invalid project role"), HttpStatus.BAD_REQUEST);
            }

            ProjectMember projectMember = projectService.addMemberToProject(newOrgMember, project, role);
            return new ResponseEntity<>(projectMember, HttpStatus.CREATED);

        }
        catch (IllegalStateException e) {
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.CONFLICT);
        }
        catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse("Unable to add project member"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
