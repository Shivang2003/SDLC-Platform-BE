package com.sdlc.sdlc.dto;

import com.sdlc.sdlc.enums.TestExecutionStatus;
import lombok.Data;

@Data
public class TestTicketExecutionStatusUpdate {

    private String testTicketKey;
    private TestExecutionStatus executionStatus;
}