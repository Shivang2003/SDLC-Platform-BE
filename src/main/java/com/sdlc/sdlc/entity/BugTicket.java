package com.sdlc.sdlc.entity;

import com.sdlc.sdlc.enums.BugStatus;
import com.sdlc.sdlc.enums.Priority;
import com.sdlc.sdlc.enums.Severity;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "bug_tickets")
public class BugTicket {

    @Id
    private ObjectId id;

    private String key;              // BUG-201

    private String title;
    private String description;

    @DBRef
    private Project project;

    private ObjectId featureId;
    private ObjectId devTicketId;
    private ObjectId testCaseId;

    @DBRef
    private ProjectMember reportedBy;

    @DBRef
    private ProjectMember assignee;

    private BugStatus status;
    private Priority priority;
    private Severity severity;

    private ObjectId detectedVersionId;
    private ObjectId fixedVersionId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
