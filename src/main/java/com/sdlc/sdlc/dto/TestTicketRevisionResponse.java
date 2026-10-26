package com.sdlc.sdlc.dto;

import com.sdlc.sdlc.enums.Priority;
import com.sdlc.sdlc.enums.TestCaseStatus;
import com.sdlc.sdlc.enums.TestExecutionStatus;
import lombok.Data;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;

@Data
public class TestTicketRevisionResponse {

    private String ticketKey;
    private Integer revisionNumber;
    private String title;
    private String description;

    private String projectId;
    private String featureId;

    private TestCaseStatus testCaseStatus;
    private TestExecutionStatus executionStatus;
    private Priority priority;

    private String createdByUserName;
    private String changedByUserName;

    private LocalDateTime createdAt;
}