package com.sdlc.sdlc.entity;

import com.sdlc.sdlc.enums.ProjectRoles;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WorklogReportEntry {

    private String userId;
    private String userName;
    private String userEmail;

    private ProjectRoles projectRole;

    private String worklogText;

    private LocalDateTime submittedAt;
}