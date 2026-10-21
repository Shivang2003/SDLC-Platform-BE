package com.sdlc.sdlc.dto;

import com.sdlc.sdlc.enums.Priority;
import com.sdlc.sdlc.enums.TestCaseStatus;
import com.sdlc.sdlc.enums.TestExecutionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.bson.types.ObjectId;

@Data
@AllArgsConstructor
public class TestTicketSummaryResponse {

    private String key;
    private String title;
    private String description;
    private TestCaseStatus testCaseStatus;
    private TestExecutionStatus executionStatus;
    private Priority priority;
    private Integer currentRevision;
    private ObjectId targetVersionId;
}