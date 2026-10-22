package com.sdlc.sdlc.service;

import com.sdlc.sdlc.dto.BugTicketResponse;
import com.sdlc.sdlc.entity.*;
import com.sdlc.sdlc.enums.BugStatus;
import com.sdlc.sdlc.repository.BugTicketRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class BugTicketService {

    @Autowired
    IdGeneratorService idGeneratorService;

    @Autowired
    BugTicketRepository bugTicketRepository;

    public BugTicket createManualBugTicket(
            BugTicket bugTicket,
            TestTicket testTicket,
            ProjectMember createdBy) {

        try {
            if (bugTicket.getTitle() == null
                    || bugTicket.getTitle().isBlank()) {
                throw new IllegalArgumentException("Bug title is required");
            }

            if (bugTicket.getPriority() == null) {
                throw new IllegalArgumentException("Priority is required");
            }

            if (bugTicket.getSeverity() == null) {
                throw new IllegalArgumentException("Severity is required");
            }

            bugTicket.setKey(idGeneratorService.generateSequenceId("bugTicket","BUG"));

            bugTicket.setTestCase(testTicket);
            bugTicket.setProject(testTicket.getProject());
            bugTicket.setFeature(testTicket.getFeature());

            bugTicket.setCreatedBy(createdBy);
            bugTicket.setAssignee(null);

            bugTicket.setStatus(BugStatus.OPEN);
            bugTicket.setCurrentRevision(1);

            LocalDateTime now = LocalDateTime.now();
            bugTicket.setCreatedAt(now);
            bugTicket.setUpdatedAt(now);

            return bugTicketRepository.save(bugTicket);

        } catch (IllegalArgumentException e) {
            throw e;

        } catch (Exception e) {
            log.error("Error creating manual BugTicket", e);
            throw new RuntimeException("Unable to create BugTicket", e);
        }
    }

    public BugTicketResponse toResponse(BugTicket bugTicket) {

        BugTicketResponse response = new BugTicketResponse();

        response.setKey(bugTicket.getKey());
        response.setTitle(bugTicket.getTitle());
        response.setDescription(bugTicket.getDescription());
        response.setStepsToReproduce(bugTicket.getStepsToReproduce());
        response.setExpectedResult(bugTicket.getExpectedResult());
        response.setActualResult(bugTicket.getActualResult());

        response.setStatus(bugTicket.getStatus());
        response.setPriority(bugTicket.getPriority());
        response.setSeverity(bugTicket.getSeverity());

        response.setTestCaseKey(
                bugTicket.getTestCase() != null
                        ? bugTicket.getTestCase().getKey()
                        : null
        );

        response.setFeatureId(
                bugTicket.getFeature() != null
                        ? bugTicket.getFeature().getFeatureId()
                        : null
        );

        response.setProjectId(
                bugTicket.getProject() != null
                        ? bugTicket.getProject().getProjectId()
                        : null
        );

        response.setCurrentRevision(bugTicket.getCurrentRevision());
        response.setCreatedAt(bugTicket.getCreatedAt());
        response.setUpdatedAt(bugTicket.getUpdatedAt());

        return response;
    }
}
