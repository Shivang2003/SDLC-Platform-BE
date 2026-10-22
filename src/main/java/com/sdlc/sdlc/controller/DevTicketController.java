package com.sdlc.sdlc.controller;


import com.sdlc.sdlc.dto.DevTicketDetailsResponse;
import com.sdlc.sdlc.dto.EditDevTicketRequest;
import com.sdlc.sdlc.dto.TestTicketSummaryResponse;
import com.sdlc.sdlc.entity.*;
import com.sdlc.sdlc.enums.ProjectRoles;
import com.sdlc.sdlc.enums.TicketStatus;
import com.sdlc.sdlc.repository.DevTicketRepository;
import com.sdlc.sdlc.repository.ProjectRepository;
import com.sdlc.sdlc.service.DevTicketService;
import com.sdlc.sdlc.service.FeatureService;
import com.sdlc.sdlc.service.TestTicketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveDatabaseCallback;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import java.util.LinkedHashMap;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/dev-ticket")
@Slf4j
public class DevTicketController {

    @Autowired
    private DevTicketRepository devTicketRepository;

    @Autowired
    FeatureService featureService;

    @Autowired
    DevTicketService devTicketService;

    @Autowired
    TestTicketService testTicketService;

    @Autowired
    ProjectRepository projectRepository;

    @PatchMapping("/{ticketKey}/status")
    public ResponseEntity<?> updateDevTicketStatus(@PathVariable String ticketKey, @RequestBody Map<String, TicketStatus> request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();

            DevTicket devTicket = devTicketRepository.findByKey(ticketKey);
            if (devTicket == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("Dev ticket not found"));
            }

            Project devTicketProject = devTicket.getProject();

            ProjectMember projectMember = featureService.validateFeatureEditAccess(devTicketProject.getProjectId(), userName, Set.of(ProjectRoles.VIEWER,ProjectRoles.TESTER));

            if (projectMember == null) {
                return ResponseEntity.status(403).body("You cannot update a dev ticket for this feature");
            }

            TicketStatus status = request.get("status");

            if (status == null) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("Status is required"));
            }

            DevTicket updatedTicket =
                    devTicketService.updateDevTicketStatus(
                            devTicket,
                            status,
                            projectMember
                    );

            return new ResponseEntity<>(updatedTicket,HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error updating dev ticket status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("Unable to update dev ticket status"));
        }
    }

    @GetMapping("/{ticketKey}/details")
    public ResponseEntity<?> getDevTicketDetails(
            @PathVariable String ticketKey) {

        try {
            Authentication authentication =
                    SecurityContextHolder.getContext().getAuthentication();

            String userName = authentication.getName();

            DevTicket devTicket = devTicketRepository.findByKey(ticketKey);

            if (devTicket == null) {
                return new ResponseEntity<>(
                        new ErrorResponse("Dev ticket not found"),
                        HttpStatus.NOT_FOUND
                );
            }

            Project project = devTicket.getProject();

            featureService.validateFeatureEditAccess(
                    project.getProjectId(),
                    userName,
                    Set.of()
            );

            List<TestTicket> testTickets =
                    testTicketService.getTestTicketsLinkedToDevTicket(
                            devTicket.getKey()
                    );

            DevTicketDetailsResponse devTicketResponse =
                    new DevTicketDetailsResponse(
                            devTicket.getKey(),
                            devTicket.getTitle(),
                            devTicket.getDescription(),
                            devTicket.getStatus(),
                            devTicket.getPriority(),
                            devTicket.getCurrentRevision(),
                            devTicket.getTargetVersionId(),
                            devTicket.getProject().getProjectId(),
                            devTicket.getFeature().getFeatureId()
                    );

            List<TestTicketSummaryResponse> testTicketResponses =
                    testTickets.stream()
                            .map(testTicket -> new TestTicketSummaryResponse(
                                    testTicket.getKey(),
                                    testTicket.getTitle(),
                                    testTicket.getDescription(),
                                    testTicket.getTestCaseStatus(),
                                    testTicket.getExecutionStatus(),
                                    testTicket.getPriority(),
                                    testTicket.getCurrentRevision(),
                                    testTicket.getTargetVersionId()
                            ))
                            .toList();

            Map<String, Object> response = new HashMap<>();
            response.put("devTicket", devTicketResponse);
            response.put("testTickets", testTicketResponses);

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(
                    new ErrorResponse(e.getMessage()),
                    HttpStatus.FORBIDDEN
            );

        } catch (Exception e) {
            log.error("Error fetching DevTicket details", e);

            return new ResponseEntity<>(
                    new ErrorResponse("Unable to fetch DevTicket details"),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @GetMapping("/{projectId}/{featureId}/dev-tickets")
    public ResponseEntity<?> getDevTicketsByFeature(@PathVariable String projectId, @PathVariable String featureId) {
        try {
            Project project = projectRepository.findByProjectId(projectId);
            if (project == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("Project not found"));
            }

            Feature feature = featureService.checkIfFeatureIdExists(featureId);
            if (feature == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("Feature not found"));
            }

            if (!feature.getProject().getId().equals(project.getId())) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Feature does not belong to this project"));
            }

            List<DevTicket> devTickets = devTicketRepository.findByProjectAndFeature(project, feature);

            Map<String, String> response = devTickets.stream()
                    .collect(Collectors.toMap(
                            DevTicket::getKey,
                            DevTicket::getTitle,
                            (existing, replacement) -> existing,
                            LinkedHashMap::new
                    ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error fetching dev tickets", e);return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("Error fetching dev tickets"));
        }
    }

    @PatchMapping("/{ticketKey}/edit")
    public ResponseEntity<?> editDevTicket(@PathVariable String ticketKey, @RequestBody EditDevTicketRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();
            DevTicket updatedTicket = devTicketService.editDevTicket(ticketKey, request, userName);
            return new ResponseEntity<>(updatedTicket, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST
            );
        }
    }

}
