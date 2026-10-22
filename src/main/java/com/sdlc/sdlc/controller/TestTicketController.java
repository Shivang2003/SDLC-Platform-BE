package com.sdlc.sdlc.controller;
import com.sdlc.sdlc.dto.BugTicketResponse;
import com.sdlc.sdlc.dto.BugTicketSummaryResponse;
import com.sdlc.sdlc.dto.CreateBugTicketRequest;
import com.sdlc.sdlc.dto.TestTicketDetailsResponse;
import com.sdlc.sdlc.entity.*;
import com.sdlc.sdlc.enums.ProjectRoles;
import com.sdlc.sdlc.enums.TestCaseStatus;
import com.sdlc.sdlc.enums.TestExecutionStatus;
import com.sdlc.sdlc.enums.TicketStatus;
import com.sdlc.sdlc.repository.BugTicketRepository;
import com.sdlc.sdlc.repository.DevTicketRepository;
import com.sdlc.sdlc.repository.TestTicketDevTicketLinkRepository;
import com.sdlc.sdlc.repository.TestTicketRepository;
import com.sdlc.sdlc.service.BugTicketService;
import com.sdlc.sdlc.service.FeatureService;
import com.sdlc.sdlc.service.TestTicketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/test-ticket")
@Slf4j
public class TestTicketController {

    @Autowired
    TestTicketRepository testTicketRepository;

    @Autowired
    TestTicketDevTicketLinkRepository testTicketDevTicketLinkRepository;

    @Autowired
    DevTicketRepository devTicketRepository;

    @Autowired
    FeatureService featureService;

    @Autowired
    BugTicketService bugTicketService;

    @Autowired
    BugTicketRepository bugTicketRepository;

    @Autowired
    TestTicketService testTicketService;


    @PostMapping("/{testTicketKey}/create-manual-bugs")
    public ResponseEntity<?> createBugTicketManually(
            @PathVariable String testTicketKey,
            @RequestBody CreateBugTicketRequest request) {

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();

            TestTicket testTicket = testTicketRepository.findByKey(testTicketKey);

            if (testTicket == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("Test ticket not found"));
            }
            BugTicket existingBug = bugTicketRepository.findByTitle(request.getTitle());
            if (existingBug != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("Bug Ticket already exists"));
            }

            TestTicketDevTicketLink link = testTicketDevTicketLinkRepository.findByTestCaseId(testTicketKey);

            if (link == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("No DevTicket is linked to this TestTicket"));
            }

            DevTicket devTicket = devTicketRepository.findByKey(link.getDevTicketId());

            if (devTicket == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("Dev ticket not found"));
            }

            Project project = devTicket.getProject();
            ProjectMember createdBy = featureService.validateFeatureEditAccess(project.getProjectId(), userName, Set.of(ProjectRoles.VIEWER, ProjectRoles.DEV));

            if (createdBy == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse("You cannot create a bug ticket for this project"));
            }

            BugTicket bugTicket = bugTicketService.mapToBugTicket(request, testTicket.getFeature(), testTicket);
            BugTicket savedBugTicket = bugTicketService.createManualBugTicket(bugTicket, testTicket, createdBy);
            BugTicketResponse response = bugTicketService.toResponse(savedBugTicket);

            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(new ErrorResponse(e.getReason()));

        } catch (Exception e) {
            log.error("Error creating manual BugTicket", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("Error creating bug ticket"));
        }
    }


    @PatchMapping("/{ticketKey}/status")
    public ResponseEntity<?> updateTestTicketStatus(@PathVariable String ticketKey, @RequestBody Map<String, TestExecutionStatus> request){
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();

            TestTicket testTicket = testTicketRepository.findByKey(ticketKey);
            if (testTicket == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("test ticket not found"));
            }
            Project devTicketProject = testTicket.getProject();

            ProjectMember projectMember = featureService.validateFeatureEditAccess(devTicketProject.getProjectId(), userName, Set.of(ProjectRoles.VIEWER,ProjectRoles.DEV));

            if (projectMember == null) {
                return ResponseEntity.status(403).body("You cannot update a test ticket for this feature");
            }

            TestExecutionStatus status = request.get("executionStatus");
            if (status == null) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Execution Status is required"));
            }

            TestTicket updatedTicket =
                    testTicketService.updateTestTicketStatus(
                            testTicket,
                            status,
                            projectMember
                    );

            return new ResponseEntity<>(updatedTicket,HttpStatus.OK);
        }
        catch (Exception e) {
        log.error("Error updating dev ticket status", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("Unable to update dev ticket status"));
        }
    }

    @GetMapping("/{ticketKey}/details")
    public ResponseEntity<?> getTestTicketDetails(@PathVariable String ticketKey) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();

            TestTicket testTicket = testTicketRepository.findByKey(ticketKey);

            if (testTicket == null) {
                return new ResponseEntity<>(new ErrorResponse("Test ticket not found"), HttpStatus.NOT_FOUND);
            }

            Project project = testTicket.getProject();
            featureService.validateFeatureEditAccess(project.getProjectId(), userName, Set.of());

            List<BugTicket> bugTickets = bugTicketRepository.findByTestCase_Id(testTicket.getId());

            TestTicketDetailsResponse testTicketResponse =
                    new TestTicketDetailsResponse(
                            testTicket.getKey(),
                            testTicket.getTitle(),
                            testTicket.getDescription(),
                            testTicket.getTestCaseStatus(),
                            testTicket.getExecutionStatus(),
                            testTicket.getPriority(),
                            testTicket.getCurrentRevision(),
                            testTicket.getTargetVersionId(),
                            testTicket.getProject().getProjectId(),
                            testTicket.getFeature().getFeatureId()
                    );

            List<BugTicketSummaryResponse> bugTicketResponses = bugTickets.stream()
                            .map(bugTicket -> new BugTicketSummaryResponse(
                                    bugTicket.getKey(),
                                    bugTicket.getTitle(),
                                    bugTicket.getDescription(),
                                    bugTicket.getStatus(),
                                    bugTicket.getPriority(),
                                    bugTicket.getSeverity(),
                                    bugTicket.getCurrentRevision(),
                                    bugTicket.getTargetVersionId()
                            ))
                            .toList();

            Map<String, Object> response = new HashMap<>();
            response.put("testTicket", testTicketResponse);
            response.put("bugTickets", bugTicketResponses);

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            log.error("Error fetching TestTicket details", e);
            return new ResponseEntity<>(new ErrorResponse("Unable to fetch TestTicket details"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}