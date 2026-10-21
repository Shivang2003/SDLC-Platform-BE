package com.sdlc.sdlc.dto;

import com.sdlc.sdlc.entity.ProjectMember;
import com.sdlc.sdlc.enums.BugStatus;
import com.sdlc.sdlc.enums.Priority;
import com.sdlc.sdlc.enums.Severity;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BugTicketResponse {

    private String key;
    private String title;
    private String description;
    private String stepsToReproduce;
    private String expectedResult;
    private String actualResult;

    private BugStatus status;
    private Priority priority;
    private Severity severity;

    private String testCaseKey;
    private String featureId;
    private String projectId;

    private Integer currentRevision;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}