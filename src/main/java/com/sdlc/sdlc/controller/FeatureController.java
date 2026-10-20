package com.sdlc.sdlc.controller;

import com.sdlc.sdlc.entity.*;
import com.sdlc.sdlc.enums.ProjectRoles;
import com.sdlc.sdlc.service.DevTicketService;
import com.sdlc.sdlc.service.FeatureService;
import com.sdlc.sdlc.service.TestTicketService;
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
@RequestMapping("/feature")
@Slf4j
public class FeatureController {

    @Autowired
    FeatureService featureService;

    @Autowired
    DevTicketService devTicketService;

    @Autowired
    TestTicketService testTicketService;

    @PostMapping("/{featureId}/create-dev-ticket")
    public ResponseEntity<?> createDevTicket(@PathVariable String featureId, @RequestBody DevTicket devTicket){
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();
            Feature feature = featureService.checkIfFeatureIdExists(featureId);
            if(feature == null){
                return ResponseEntity.status(404).body("Feature doesn't exist");
            }
            DevTicket devTicketInDb = devTicketService.checkIfDevTicketNameExists(devTicket.getTitle(), feature);
            if(devTicketInDb!=null){
                return new ResponseEntity<>(new ErrorResponse("Dev Ticket already exists"), HttpStatus.CONFLICT);
            }
            String projectId = feature.getProject().getProjectId();
            ProjectMember projectMember = featureService.validateFeatureEditAccess(projectId, userName, Set.of(ProjectRoles.VIEWER, ProjectRoles.TESTER));
            if (projectMember == null) {
                return ResponseEntity.status(403).body("You cannot create a dev ticket for this feature");
            }
            devTicketService.createDevTicket(devTicket,feature,projectMember);
            return new ResponseEntity<>(HttpStatus.CREATED);
        }
        catch(Exception e){
            log.error("Error creating feature: ", e);
            return ResponseEntity.badRequest().body("Error creating ticket");
        }
    }
    @PostMapping("/{featureId}/{devTicketId}/create-test-ticket")
    public ResponseEntity<?> createTestTicket(@PathVariable String featureId, @PathVariable String devTicketId, @RequestBody TestTicket testTicket) {

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();

            Feature feature = featureService.checkIfFeatureIdExists(featureId);
            if (feature == null) {
                return ResponseEntity.status(404).body("Feature doesn't exist");
            }
            TestTicket testTicketInDb = testTicketService.checkIfTestTicketNameExists(testTicket.getTitle(), feature);

            if (testTicketInDb != null) {
                return new ResponseEntity<>(new ErrorResponse("Test Ticket already exists"), HttpStatus.CONFLICT);
            }

            String projectId = feature.getProject().getProjectId();

            ProjectMember projectMember = featureService.validateFeatureEditAccess(projectId, userName, Set.of(ProjectRoles.VIEWER));

            if (projectMember == null) {
                return ResponseEntity.status(403).body("You cannot create a test ticket for this feature");
            }

            DevTicket devTicket = devTicketService.checkIfDevTicketKeyExists(devTicketId);
            if (devTicket == null){
                return ResponseEntity.status(403).body("Provided dev ticket doesn't exist");
            }

            TestTicket createdTestTicket = testTicketService.createTestTicket(testTicket, feature, projectMember);

            testTicketService.createTestTicketDevTicketLink(
                    createdTestTicket, devTicket);

            return new ResponseEntity<>(createdTestTicket,HttpStatus.CREATED);

        } catch (Exception e) {
            log.error("Error creating test ticket: ", e);
            return ResponseEntity.badRequest()
                    .body("Error creating test ticket");
        }
    }
}
