package com.sdlc.sdlc.entity;

import com.sdlc.sdlc.enums.TestExecutionStatus;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "test_executions")
public class TestExecution {

    @Id
    private ObjectId id;

    private ObjectId testCaseId;
    private ObjectId projectVersionId;

    @DBRef
    private ProjectMember executedBy;

    private TestExecutionStatus result;

    private String remarks;

    private LocalDateTime executedAt;
}