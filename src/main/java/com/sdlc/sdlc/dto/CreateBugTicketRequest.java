package com.sdlc.sdlc.dto;

import com.sdlc.sdlc.enums.BugStatus;
import com.sdlc.sdlc.enums.Priority;
import com.sdlc.sdlc.enums.Severity;
import lombok.Data;
import org.bson.types.ObjectId;

@Data
public class CreateBugTicketRequest {

    private String title;
    private String description;
    private String stepsToReproduce;
    private String expectedResult;
    private String actualResult;
    private String assignee;
    private BugStatus status;
    private Priority priority;
    private Severity severity;
    private ObjectId targetVersionId;
}