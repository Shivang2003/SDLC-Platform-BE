package com.sdlc.sdlc.entity;

import com.sdlc.sdlc.enums.Priority;
import com.sdlc.sdlc.enums.TestCaseStatus;
import com.sdlc.sdlc.enums.TestExecutionStatus;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "test_cases")
public class TestTicket {

    @Id
    private ObjectId id;

    @Indexed(unique = true)
    private String key;              // TC-001

    private String title;
    private String description;

    @DBRef
    private Project project;

    @DBRef
    private Feature feature;

    private TestExecutionStatus executionStatus;
    private TestCaseStatus testCaseStatus;

    @DBRef
    private ProjectMember createdBy;

    private ObjectId targetVersionId;
    private Priority priority;
    private Integer currentRevision;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
