package com.sdlc.sdlc.dto;

import com.sdlc.sdlc.enums.ProjectRoles;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class WorklogResponse {

    private String worklogId;
    private String projectId;
    private String userId;
    private String userName;
    private ProjectRoles projectRole;
    private LocalDate workDate;
    private String worklogText;
    private LocalDateTime submittedAt;
}
