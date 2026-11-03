package com.sdlc.sdlc.entity;

import com.sdlc.sdlc.enums.ProjectRoles;
import com.sdlc.sdlc.enums.WorklogStatus;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "worklogs")
@Data
public class Worklog {

    @Id
    private String id;

    private String worklogId;
    private String projectId;
    private String userId;
    private String userName;
    private String userEmail;
    private ProjectRoles projectRole;
    private String worklogText;
    private LocalDate workDate;
    private LocalDateTime submittedAt;
    private WorklogStatus status;
}
