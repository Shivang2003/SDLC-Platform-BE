package com.sdlc.sdlc.service;

import com.sdlc.sdlc.dto.*;
import com.sdlc.sdlc.entity.*;
import com.sdlc.sdlc.enums.*;
import com.sdlc.sdlc.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.sdlc.sdlc.enums.Priority;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TestTicketService {

    @Autowired
    IdGeneratorService idGeneratorService;

    @Autowired
    TestTicketRepository testTicketRepository;

    @Autowired
    TestTicketRevisionRepository testTicketRevisionRepository;

    @Autowired
    TestTicketDevTicketLinkRepository testTicketDevTicketLinkRepository;

    @Autowired
    ProjectMemberRepository projectMemberRepository;

    @Autowired
    OrganizationMemberRepository organizationMemberRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    FeatureService featureService;

    @Autowired
    TestTicketStatusHistoryRepository testTicketStatusHistoryRepository;

    public TestTicket checkIfTestTicketNameExists(String name, Feature feature){
        try{
            TestTicket testTicket = testTicketRepository.findByTitleAndFeature(name, feature);
            return testTicket;
        }
        catch (Exception e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error checking if feature exists", e);
        }
    }
    public TestTicket createTestTicket(
            TestTicket testTicket,
            Feature feature,
            ProjectMember createdBy) {

        try {
            LocalDateTime now = LocalDateTime.now();

            testTicket.setKey(
                    idGeneratorService.generateSequenceId("testTicket", "TC"));

            testTicket.setFeature(feature);
            testTicket.setProject(feature.getProject());
            testTicket.setCreatedBy(createdBy);

            testTicket.setExecutionStatus(testTicket.getExecutionStatus() != null ? testTicket.getExecutionStatus() : TestExecutionStatus.NOT_EXECUTED);
            testTicket.setTestCaseStatus(testTicket.getTestCaseStatus() != null ? testTicket.getTestCaseStatus() : TestCaseStatus.DRAFT);
            testTicket.setPriority(testTicket.getPriority() != null ? testTicket.getPriority() : Priority.LOW);
            testTicket.setTargetVersionId(feature.getTargetVersionId());
            testTicket.setCurrentRevision(1);

            testTicket.setCreatedAt(now);
            testTicket.setUpdatedAt(now);

            TestTicket savedTicket = testTicketRepository.save(testTicket);

            createRevision(savedTicket, createdBy);

            return savedTicket;
        }
        catch (Exception e) {
            log.error(
                    "Error creating revision for TestTicket {}",
                    testTicket.getKey(), e);

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to create TestTicket revision",
                    e);
        }
    }


    public TestTicket mapToTestTicket(
            CreateTestTicketRequest request,
            Feature feature) {

        TestTicket testTicket = new TestTicket();

        testTicket.setTitle(request.getTitle());
        testTicket.setDescription(request.getDescription());

        testTicket.setTestCaseStatus(request.getTestCaseStatus());
        testTicket.setExecutionStatus(request.getExecutionStatus());
        testTicket.setPriority(request.getPriority());

        testTicket.setFeature(feature);
        testTicket.setProject(feature.getProject());

        return testTicket;
    }


    public TestTicketRevision createRevision(
            TestTicket testTicket,
            ProjectMember changedBy) {

        TestTicketRevision revision = new TestTicketRevision();

        revision.setTicketKey(testTicket.getKey());
        revision.setRevisionNumber(testTicket.getCurrentRevision());

        revision.setTestTicket(testTicket);

        revision.setTitle(testTicket.getTitle());
        revision.setDescription(testTicket.getDescription());

        revision.setProject(testTicket.getProject());
        revision.setFeature(testTicket.getFeature());

        revision.setTestCaseStatus(testTicket.getTestCaseStatus());
        revision.setExecutionStatus(testTicket.getExecutionStatus());

        revision.setCreatedBy(testTicket.getCreatedBy());
        revision.setTargetVersionId(testTicket.getTargetVersionId());
        revision.setPriority(testTicket.getPriority());

        revision.setChangedBy(changedBy);
        revision.setCreatedAt(LocalDateTime.now());

        return testTicketRevisionRepository.save(revision);
    }

    public TestTicketDevTicketLink createTestTicketDevTicketLink(
            TestTicket testTicket,
            DevTicket devTicket) {

        TestTicketDevTicketLink link =
                new TestTicketDevTicketLink();

        link.setTestCaseId(testTicket.getKey().toString());
        link.setDevTicketId(devTicket.getKey().toString());
        link.setCreatedAt(LocalDateTime.now());

        return testTicketDevTicketLinkRepository.save(link);
    }

    public List<TestTicket> getTestTicketsLinkedToDevTicket(String devTicketId) {
        List<TestTicketDevTicketLink> links = testTicketDevTicketLinkRepository.findByDevTicketId(devTicketId);

        return links.stream()
                .map(link -> testTicketRepository.findByKey(link.getTestCaseId()))
                .filter(Objects::nonNull)
                .toList();
    }

    public TestTicket updateTestTicketStatus(
            TestTicket testTicket,
            TestExecutionStatus newStatus,
            ProjectMember changedBy) {

        try {
            TestExecutionStatus previousStatus = testTicket.getExecutionStatus();

            if (previousStatus == newStatus) {
                return testTicket;
            }

            LocalDateTime now = LocalDateTime.now();

            // Update test ticket
            testTicket.setExecutionStatus(newStatus);
            testTicket.setUpdatedAt(now);
            testTicket.setCurrentRevision(
                    testTicket.getCurrentRevision() + 1
            );

            TestTicket updatedTicket = testTicketRepository.save(testTicket);

            // Create status history
            TestTicketStatusHistory history =
                    new TestTicketStatusHistory();

            history.setTestTicket(updatedTicket);
            history.setPreviousStatus(previousStatus);
            history.setNewStatus(newStatus);
            history.setChangedBy(changedBy);
            history.setChangedAt(now);

            testTicketStatusHistoryRepository.save(history);

            // Create revision snapshot
            createRevision(updatedTicket, changedBy);

            return updatedTicket;

        } catch (Exception e) {
            log.error(
                    "Error updating status for TestTicket {}",
                    testTicket.getKey(),
                    e
            );

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to update TestTicket status",
                    e
            );
        }
    }
    @Transactional(rollbackFor = Exception.class)
    public void updateExecutionStatusesForTestTicket(String devTicketKey, List<TestTicketExecutionStatusUpdate> updates, ProjectMember changedBy) {
        try {
            if (updates == null || updates.isEmpty()) {
                return;
            }

            List<TestTicket> testTickets = getTestTicketsLinkedToDevTicket(devTicketKey);
            Map<String, TestTicket> linkedTickets = testTickets.stream().collect(Collectors.toMap(TestTicket::getKey, Function.identity()));

            Set<String> requestedKeys = new HashSet<>();

            // Validate everything before updating any TestTicket
            for (TestTicketExecutionStatusUpdate update : updates) {
                if (update.getTestTicketKey() == null || update.getExecutionStatus() == null) {throw new IllegalArgumentException("TestTicket key and execution status are required");
                }

                if (!requestedKeys.add(update.getTestTicketKey())) {
                    throw new IllegalArgumentException("Duplicate TestTicket: " + update.getTestTicketKey());
                }

                if (!linkedTickets.containsKey(update.getTestTicketKey())) {
                    throw new IllegalArgumentException("TestTicket " + update.getTestTicketKey() + " is not linked to " + devTicketKey);
                }
            }

            // Update each TestTicket using your existing status-update method
            for (TestTicketExecutionStatusUpdate update : updates) {
                TestTicket testTicket = linkedTickets.get(update.getTestTicketKey());
                updateTestTicketStatus(testTicket, update.getExecutionStatus(), changedBy);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to update TestTicket statuses for " + devTicketKey, e);
        }
    }


    @Transactional(rollbackFor = Exception.class)
    public TestTicket editTestTicket(String ticketKey, EditTestTicketRequest request, String userName) {

        TestTicket testTicket = testTicketRepository.findByKey(ticketKey);
        if (testTicket == null) {
            throw new RuntimeException("TestTicket not found");
        }

        featureService.validateFeatureEditAccess(testTicket.getProject().getProjectId(), userName, Set.of(ProjectRoles.DEV, ProjectRoles.VIEWER));
        User user = userRepository.findByUserName(userName);

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        OrganizationMember organizationMember = organizationMemberRepository.findByUserAndOrganization(user, testTicket.getProject().getOrganization());

        if (organizationMember == null) {
            throw new RuntimeException("Organization member not found");
        }

        ProjectMember changedBy = projectMemberRepository.findByProjectAndOrganizationMember(
                        testTicket.getProject(),
                        organizationMember
                ).orElseThrow(() -> new RuntimeException("Project member not found"));

        boolean ticketChanged = false;

        // Update title
        if (request.getTitle() != null && !request.getTitle().equals(testTicket.getTitle())) {
            TestTicket existingTicket = checkIfTestTicketNameExists(request.getTitle(), testTicket.getFeature());
            if (existingTicket != null && !existingTicket.getKey().equals(testTicket.getKey())) {
                throw new RuntimeException("TestTicket with this title already exists in this feature");
            }
            testTicket.setTitle(request.getTitle());
            ticketChanged = true;
        }

        // Update description
        if (request.getDescription() != null && !Objects.equals(request.getDescription(), testTicket.getDescription())) {

            testTicket.setDescription(request.getDescription());
            ticketChanged = true;
        }

        // Update priority
        if (request.getPriority() != null
                && request.getPriority() != testTicket.getPriority()) {

            testTicket.setPriority(request.getPriority());
            ticketChanged = true;
        }

        // Update test case status
        if (request.getTestCaseStatus() != null
                && request.getTestCaseStatus() != testTicket.getTestCaseStatus()) {

            testTicket.setTestCaseStatus(request.getTestCaseStatus());
            ticketChanged = true;
        }

        // Update execution status
        if (request.getExecutionStatus() != null
                && request.getExecutionStatus() != testTicket.getExecutionStatus()) {

            testTicket.setExecutionStatus(request.getExecutionStatus());
            ticketChanged = true;
        }

        if (request.getAssigneeUserName() != null) {
            User assigneeUser = userRepository.findByUserName(request.getAssigneeUserName());
            if (assigneeUser == null) {
                throw new RuntimeException("Assignee user not found");
            }
            OrganizationMember assigneeOrganizationMember = organizationMemberRepository.findByUserAndOrganization(assigneeUser, testTicket.getProject().getOrganization());
            if (assigneeOrganizationMember == null) {
                throw new RuntimeException("Assignee is not a member of this organization");
            }
            ProjectMember assignee = projectMemberRepository.findByProjectAndOrganizationMember(
                    testTicket.getProject(),
                    assigneeOrganizationMember).orElseThrow(() -> new RuntimeException("Assignee is not a member of this project"));
            testTicket.setAssignee(assignee);
        }

        if (ticketChanged) {
            testTicket.setUpdatedAt(LocalDateTime.now());
            testTicket.setUpdatedBy(changedBy);

            if (testTicket.getCurrentRevision() == null) {
                testTicket.setCurrentRevision(1);
            } else {
                testTicket.setCurrentRevision(
                        testTicket.getCurrentRevision() + 1);
            }

            TestTicket updatedTicket = testTicketRepository.save(testTicket);

            createRevision(updatedTicket, changedBy);

            return updatedTicket;
        }

        return testTicket;
    }

    public List<TestTicketRevisionResponse> getTestTicketRevisions(
            String ticketKey) {

        TestTicket testTicket = testTicketRepository.findByKey(ticketKey);

        if (testTicket == null) {
            throw new RuntimeException("TestTicket not found");
        }

        List<TestTicketRevision> revisions =
                testTicketRevisionRepository
                        .findByTicketKeyOrderByRevisionNumberDesc(ticketKey);

        return revisions.stream().map(revision -> {
            TestTicketRevisionResponse response =
                    new TestTicketRevisionResponse();

            response.setTicketKey(revision.getTicketKey());
            response.setRevisionNumber(revision.getRevisionNumber());
            response.setTitle(revision.getTitle());
            response.setDescription(revision.getDescription());
            response.setTestCaseStatus(revision.getTestCaseStatus());
            response.setExecutionStatus(revision.getExecutionStatus());
            response.setPriority(revision.getPriority());
            response.setCreatedAt(revision.getCreatedAt());

            if (revision.getProject() != null) {
                response.setProjectId(
                        revision.getProject().getProjectId());
            }

            if (revision.getFeature() != null) {
                response.setFeatureId(
                        revision.getFeature().getFeatureId());
            }

            if (revision.getCreatedBy() != null
                    && revision.getCreatedBy().getOrganizationMember() != null
                    && revision.getCreatedBy().getOrganizationMember().getUser() != null) {
                response.setCreatedByUserName(
                        revision.getCreatedBy().getOrganizationMember()
                                .getUser().getUserName());
            }

            if (revision.getChangedBy() != null
                    && revision.getChangedBy().getOrganizationMember() != null
                    && revision.getChangedBy().getOrganizationMember().getUser() != null) {
                response.setChangedByUserName(
                        revision.getChangedBy().getOrganizationMember()
                                .getUser().getUserName());
            }

            return response;
        }).toList();
    }
}
