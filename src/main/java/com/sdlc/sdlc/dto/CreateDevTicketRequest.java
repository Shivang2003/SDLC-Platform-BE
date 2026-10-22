package com.sdlc.sdlc.dto;

import com.sdlc.sdlc.enums.Priority;
import com.sdlc.sdlc.enums.TicketStatus;
import lombok.Data;

import org.bson.types.ObjectId;

@Data
public class CreateDevTicketRequest {
    private String title;
    private String description;
    private String assignee;
    private Priority priority;
    private ObjectId targetVersionId;
    private TicketStatus status;
}