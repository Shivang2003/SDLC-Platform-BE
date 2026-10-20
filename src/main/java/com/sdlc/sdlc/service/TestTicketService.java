package com.sdlc.sdlc.service;

import com.sdlc.sdlc.entity.*;
import com.sdlc.sdlc.enums.*;
import com.sdlc.sdlc.repository.TestTicketDevTicketLinkRepository;
import com.sdlc.sdlc.repository.TestTicketRepository;
import com.sdlc.sdlc.repository.TestTicketRevisionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.sdlc.sdlc.enums.Priority;

import java.time.LocalDateTime;

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

            testTicket.setExecutionStatus(TestExecutionStatus.NOT_EXECUTED);
            testTicket.setTestCaseStatus(TestCaseStatus.DRAFT);
            testTicket.setPriority(Priority.MEDIUM);
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
}
