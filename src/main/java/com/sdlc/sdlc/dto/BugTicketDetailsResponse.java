package com.sdlc.sdlc.dto;

import com.sdlc.sdlc.enums.BugStatus;
import com.sdlc.sdlc.enums.Priority;
import com.sdlc.sdlc.enums.Severity;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.bson.types.ObjectId;

@Data
@AllArgsConstructor
public class BugTicketDetailsResponse {

    private String key;
    private String title;
    private String description;
    private String stepsToReproduce;
    private String expectedResult;
    private String actualResult;
    private BugStatus status;
    private Priority priority;
    private Severity severity;
    private Integer currentRevision;
    private ObjectId targetVersionId;
    private String projectName;
    private String featureName;
    private String testCaseKey;
}
