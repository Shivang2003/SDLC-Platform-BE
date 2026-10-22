package com.sdlc.sdlc.dto;

import com.sdlc.sdlc.enums.Priority;
import lombok.Data;

@Data
public class EditDevTicketRequest {

    private String title;
    private String description;
    private Priority priority;
    private String targetVersionId;
    private String assigneeId;
}