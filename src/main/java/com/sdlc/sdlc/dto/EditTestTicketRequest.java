package com.sdlc.sdlc.dto;

import ch.qos.logback.classic.spi.Configurator;
import com.sdlc.sdlc.entity.ProjectMember;
import com.sdlc.sdlc.enums.Priority;
import com.sdlc.sdlc.enums.TestCaseStatus;
import com.sdlc.sdlc.enums.TestExecutionStatus;
import lombok.Data;

import java.util.List;

@Data
public class EditTestTicketRequest {

    private String title;
    private String description;
    private Priority priority;
    private TestCaseStatus testCaseStatus;
    private TestExecutionStatus executionStatus;
    private String assigneeUserName;

    private List<BugTicketStatusUpdate> bugStatuses;
}