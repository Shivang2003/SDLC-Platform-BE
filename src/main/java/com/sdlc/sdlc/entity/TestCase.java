package com.sdlc.sdlc.entity;

import com.sdlc.sdlc.enums.TestExecutionStatus;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "test_cases")
public class TestCase {

    @Id
    private ObjectId id;

    private String key;              // TC-001

    private String title;
    private String description;

    @DBRef
    private Project project;

    @DBRef
    private Feature feature;

    private TestExecutionStatus status;

    @DBRef
    private ProjectMember createdBy;

    private ObjectId targetVersionId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
