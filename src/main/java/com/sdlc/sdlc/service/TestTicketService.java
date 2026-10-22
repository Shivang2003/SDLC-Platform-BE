package com.sdlc.sdlc.service;

import com.sdlc.sdlc.dto.CreateTestTicketRequest;
import com.sdlc.sdlc.entity.*;
import com.sdlc.sdlc.enums.*;
import com.sdlc.sdlc.repository.TestTicketDevTicketLinkRepository;
import com.sdlc.sdlc.repository.TestTicketRepository;
import com.sdlc.sdlc.repository.TestTicketRevisionRepository;
import com.sdlc.sdlc.repository.TestTicketStatusHistoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.sdlc.sdlc.enums.Priority;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

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
    private TestTicketStatusHistoryRepository testTicketStatusHistoryRepository;

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
}
