package com.sdlc.sdlc.controller;

import com.sdlc.sdlc.dto.AddOrganizationMemberRequest;
import com.sdlc.sdlc.entity.DevTicket;
import com.sdlc.sdlc.entity.ErrorResponse;
import com.sdlc.sdlc.entity.Feature;
import com.sdlc.sdlc.entity.ProjectMember;
import com.sdlc.sdlc.enums.ProjectRoles;
import com.sdlc.sdlc.repository.FeatureRepositroy;
import com.sdlc.sdlc.service.DevTicketService;
import com.sdlc.sdlc.service.FeatureService;
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

import java.util.Set;

@Controller
@RequestMapping("/feature")
@Slf4j
public class FeatureController {

    @Autowired
    FeatureService featureService;

    @Autowired
    DevTicketService devTicketService;

    @PostMapping("{featureId}/create-dev-ticket")
    public ResponseEntity<?> createDevTicket(@RequestParam String featureId, @RequestBody DevTicket devTicket){
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
}
