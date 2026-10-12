package com.sdlc.sdlc.controller;

import com.sdlc.sdlc.entity.*;
import com.sdlc.sdlc.enums.ProjectRoles;
import com.sdlc.sdlc.service.FeatureService;
import com.sdlc.sdlc.service.ProjectService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@Controller
@RequestMapping("/project")
@Slf4j
public class ProjectController {

    @Autowired
    ProjectService projectService;

    @Autowired
    FeatureService featureService;

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
}
