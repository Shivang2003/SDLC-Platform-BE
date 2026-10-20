package com.sdlc.sdlc.entity;

import com.sdlc.sdlc.enums.Priority;
import com.sdlc.sdlc.enums.TestCaseStatus;
import com.sdlc.sdlc.enums.TestExecutionStatus;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "test_ticket_revisions")
public class TestTicketRevision {

    @Id
    private ObjectId id;

    private String ticketKey;
    private Integer revisionNumber;

    @DBRef
    private TestTicket testTicket;

    private String title;
    private String description;

    @DBRef
    private Project project;

    @DBRef
    private Feature feature;

    private TestCaseStatus testCaseStatus;
    private TestExecutionStatus executionStatus;

    @DBRef
    private ProjectMember createdBy;

    private ObjectId targetVersionId;

    private Priority priority;

    @DBRef
    private ProjectMember changedBy;

    private LocalDateTime createdAt;
}