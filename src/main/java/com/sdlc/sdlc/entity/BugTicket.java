package com.sdlc.sdlc.entity;

import com.sdlc.sdlc.enums.BugStatus;
import com.sdlc.sdlc.enums.Priority;
import com.sdlc.sdlc.enums.Severity;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "bug_tickets")
public class BugTicket {

    @Id
    private ObjectId id;

    @Indexed(unique = true)
    private String key;

    private String title;
    private String description;

    private String stepsToReproduce;
    private String expectedResult;
    private String actualResult;

    @DBRef
    private TestTicket testCase;

    @DBRef
    private Project project;

    @DBRef
    private Feature feature;

    @DBRef
    private ProjectMember assignee;

    @DBRef
    private ProjectMember createdBy;

    private BugStatus status;
    private Priority priority;
    private Severity severity;

    private ObjectId targetVersionId;

    private Integer currentRevision;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}