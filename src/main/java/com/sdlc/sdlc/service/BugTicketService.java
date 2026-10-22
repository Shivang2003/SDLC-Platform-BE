package com.sdlc.sdlc.service;

import com.sdlc.sdlc.dto.BugTicketResponse;
import com.sdlc.sdlc.dto.CreateBugTicketRequest;
import com.sdlc.sdlc.entity.*;
import com.sdlc.sdlc.enums.BugStatus;
import com.sdlc.sdlc.enums.Priority;
import com.sdlc.sdlc.enums.Severity;
import com.sdlc.sdlc.repository.BugTicketRepository;
import com.sdlc.sdlc.repository.ProjectMemberRepository;
import com.sdlc.sdlc.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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

    @Autowired
    ProjectMemberRepository projectMemberRepository;

    @Autowired
    UserRepository userRepository;

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

    public BugTicket createBugTicket(
            BugTicket bugTicket,
            Feature feature,
            ProjectMember createdBy) {

        try {
            LocalDateTime now = LocalDateTime.now();

            bugTicket.setKey(
                    idGeneratorService.generateSequenceId("bugTicket", "BUG")
            );

            bugTicket.setFeature(feature);
            bugTicket.setProject(feature.getProject());
            bugTicket.setCreatedBy(createdBy);

            bugTicket.setStatus(
                    bugTicket.getStatus() != null
                            ? bugTicket.getStatus()
                            : BugStatus.OPEN
            );

            bugTicket.setPriority(
                    bugTicket.getPriority() != null
                            ? bugTicket.getPriority()
                            : Priority.MEDIUM
            );

            bugTicket.setSeverity(
                    bugTicket.getSeverity() != null
                            ? bugTicket.getSeverity()
                            : Severity.MEDIUM
            );

            bugTicket.setCurrentRevision(1);
            bugTicket.setCreatedAt(now);
            bugTicket.setUpdatedAt(now);

            BugTicket savedTicket = bugTicketRepository.save(bugTicket);

//            createRevision(savedTicket, createdBy);

            return savedTicket;

        } catch (Exception e) {
            log.error("Error creating BugTicket {}", bugTicket.getKey(), e);

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to create BugTicket",
                    e
            );
        }
    }

    public BugTicket mapToBugTicket(
            CreateBugTicketRequest request,
            Feature feature,
            TestTicket testTicket) {

        BugTicket bugTicket = new BugTicket();

        bugTicket.setTitle(request.getTitle());
        bugTicket.setDescription(request.getDescription());
        bugTicket.setStepsToReproduce(request.getStepsToReproduce());
        bugTicket.setExpectedResult(request.getExpectedResult());
        bugTicket.setActualResult(request.getActualResult());
        bugTicket.setStatus(request.getStatus());
        bugTicket.setPriority(request.getPriority());
        bugTicket.setSeverity(request.getSeverity());
        bugTicket.setTargetVersionId(request.getTargetVersionId());

        bugTicket.setFeature(feature);
        bugTicket.setProject(feature.getProject());
        bugTicket.setTestCase(testTicket);

        if (request.getAssignee() != null
                && !request.getAssignee().isBlank()) {

            User assignedUser =
                    userRepository.findByUserName(request.getAssignee());

            if (assignedUser == null) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Assignee user not found"
                );
            }

            List<ProjectMember> projectMembers =
                    projectMemberRepository.findByProject_Id(
                            feature.getProject().getId()
                    );

            ProjectMember assignee = projectMembers.stream()
                    .filter(member ->
                            member.getOrganizationMember() != null
                                    && member.getOrganizationMember().getUser() != null
                                    && member.getOrganizationMember().getUser()
                                    .getId().equals(assignedUser.getId()))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Assignee is not a member of this project"
                    ));

            bugTicket.setAssignee(assignee);
        }

        return bugTicket;
    }
}
