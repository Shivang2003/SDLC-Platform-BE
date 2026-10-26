package com.sdlc.sdlc.dto;

import com.sdlc.sdlc.enums.BugStatus;
import lombok.Data;

@Data
public class BugTicketStatusUpdate {
    private String bugTicketKey;
    private BugStatus status;
}