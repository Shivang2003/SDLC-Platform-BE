package com.sdlc.sdlc.controller;
import com.sdlc.sdlc.dto.BugTicketResponse;
import com.sdlc.sdlc.entity.*;
import com.sdlc.sdlc.enums.ProjectRoles;
import com.sdlc.sdlc.repository.DevTicketRepository;
import com.sdlc.sdlc.repository.TestTicketDevTicketLinkRepository;
import com.sdlc.sdlc.repository.TestTicketRepository;
import com.sdlc.sdlc.service.BugTicketService;
import com.sdlc.sdlc.service.FeatureService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/{testTicketKey}/create-manual-bugs")
    public ResponseEntity<?> createBugTicketManually(@PathVariable String testTicketKey,@RequestBody BugTicket bugTicket){
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();

            TestTicketDevTicketLink testTicketDevTicketLink = testTicketDevTicketLinkRepository.findByTestCaseId(testTicketKey);

            DevTicket devTicket = devTicketRepository.findByKey(testTicketDevTicketLink.getDevTicketId());
            if (devTicket == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("Dev ticket not found"));
            }

            Project devTicketProject = devTicket.getProject();
            ProjectMember projectMember = featureService.validateFeatureEditAccess(devTicketProject.getProjectId(), userName, Set.of(ProjectRoles.VIEWER,ProjectRoles.DEV));

            if (projectMember == null) {
                return ResponseEntity.status(403).body("You cannot create a test ticket for this feature");
            }

            TestTicket testTicket = testTicketRepository.findByKey(testTicketKey);

            if (testTicket == null) {
                return new ResponseEntity<>(
                        new ErrorResponse("Test ticket not found"),
                        HttpStatus.NOT_FOUND
                );
            }

            BugTicket savedBugTicket =
                    bugTicketService.createManualBugTicket(
                            bugTicket, testTicket, projectMember
                    );

            BugTicketResponse response =
                    bugTicketService.toResponse(savedBugTicket);

            return new ResponseEntity<>(response, HttpStatus.CREATED);

        }
        catch (Exception e) {
            log.error("Error creating test ticket: ", e);
            return ResponseEntity.badRequest()
                    .body("Error creating test ticket");
        }
    }
}