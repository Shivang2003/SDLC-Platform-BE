
package com.sdlc.sdlc.dto;

import com.sdlc.sdlc.enums.Priority;
import com.sdlc.sdlc.enums.TestCaseStatus;
import com.sdlc.sdlc.enums.TestExecutionStatus;
import lombok.Data;

@Data
public class CreateTestTicketRequest {
    private String title;
    private String description;
    private TestCaseStatus testCaseStatus;
    private TestExecutionStatus executionStatus;
    private Priority priority;
}
