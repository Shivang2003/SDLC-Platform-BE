package com.sdlc.sdlc.dto;

import com.sdlc.sdlc.enums.Priority;
import com.sdlc.sdlc.enums.TestExecutionStatus;
import lombok.Data;

import java.util.List;

@Data
public class EditDevTicketRequest {

    private String title;
    private String description;
    private Priority priority;
    private String assigneeUserName;

    private List<TestTicketExecutionStatusUpdate> testTicketStatuses;
}