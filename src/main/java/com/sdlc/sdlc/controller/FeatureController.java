package com.sdlc.sdlc.controller;

import com.sdlc.sdlc.dto.CreateDevTicketRequest;
import com.sdlc.sdlc.dto.CreateTestTicketRequest;
import com.sdlc.sdlc.entity.*;
import com.sdlc.sdlc.enums.ProjectRoles;
import com.sdlc.sdlc.repository.ProjectMemberRepository;
import com.sdlc.sdlc.repository.UserRepository;
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
import org.springframework.web.server.ResponseStatusException;

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

    @Autowired
    ProjectMemberRepository projectMemberRepository;

    @Autowired
    UserRepository userRepository;

    @PostMapping("/{featureId}/create-dev-ticket")
    public ResponseEntity<?> createDevTicket(@PathVariable String featureId, @RequestBody CreateDevTicketRequest devTicketRequest){
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();
            Feature feature = featureService.checkIfFeatureIdExists(featureId);
            if(feature == null){
                return ResponseEntity.status(404).body("Feature doesn't exist");
            }
            DevTicket devTicketInDb = devTicketService.checkIfDevTicketNameExists(devTicketRequest.getTitle(), feature);
            if(devTicketInDb!=null){
                return new ResponseEntity<>(new ErrorResponse("Dev Ticket already exists"), HttpStatus.CONFLICT);
            }
            String projectId = feature.getProject().getProjectId();
            ProjectMember projectMember = featureService.validateFeatureEditAccess(projectId, userName, Set.of(ProjectRoles.VIEWER, ProjectRoles.TESTER));
            if (projectMember == null) {
                return ResponseEntity.status(403).body("You cannot create a dev ticket for this feature");
            }

            DevTicket devTicket = devTicketService.mapToDevTicket(devTicketRequest, feature);
            if(devTicket == null){
                return ResponseEntity.badRequest().body("Please all the fields properly");
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
    public ResponseEntity<?> createTestTicket(
            @PathVariable String featureId,
            @PathVariable String devTicketId,
            @RequestBody CreateTestTicketRequest request) {

        try {
            Authentication authentication =
                    SecurityContextHolder.getContext().getAuthentication();

            String userName = authentication.getName();

            Feature feature =
                    featureService.checkIfFeatureIdExists(featureId);

            if (feature == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Feature doesn't exist"));
            }

            TestTicket existingTicket =
                    testTicketService.checkIfTestTicketNameExists(
                            request.getTitle(), feature);

            if (existingTicket != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new ErrorResponse("Test Ticket already exists"));
            }

            String projectId = feature.getProject().getProjectId();

            ProjectMember createdBy =
                    featureService.validateFeatureEditAccess(
                            projectId,
                            userName,
                            Set.of(ProjectRoles.VIEWER)
                    );

            if (createdBy == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse(
                                "You cannot create a test ticket for this feature"));
            }

            DevTicket devTicket =
                    devTicketService.checkIfDevTicketKeyExists(devTicketId);

            if (devTicket == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Dev ticket doesn't exist"));
            }

            if (!devTicket.getFeature().getFeatureId().equals(featureId)) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse(
                                "Dev ticket does not belong to this feature"));
            }

            TestTicket testTicket =
                    testTicketService.mapToTestTicket(request, feature);

            TestTicket createdTestTicket =
                    testTicketService.createTestTicket(
                            testTicket, feature, createdBy);

            testTicketService.createTestTicketDevTicketLink(
                    createdTestTicket, devTicket);

            return new ResponseEntity<>(createdTestTicket, HttpStatus.CREATED);

        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(new ErrorResponse(e.getReason()));

        } catch (Exception e) {
            log.error("Error creating TestTicket", e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error creating test ticket"));
        }
    }

}
