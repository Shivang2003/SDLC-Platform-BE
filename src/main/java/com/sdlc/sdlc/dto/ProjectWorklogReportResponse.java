package com.sdlc.sdlc.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class ProjectWorklogReportResponse {
    private String projectId;
    private String projectName;
    private LocalDate reportDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String timeZone;
    private List<WorklogResponse> worklogs;
    private List<MissingWorklogMemberResponse> missingMembers;
}
