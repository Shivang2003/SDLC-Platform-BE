package com.sdlc.sdlc.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Document(collection = "project_worklog_config")
@Data
public class ProjectWorklogConfig {

    @Id
    private String id;

    private String workLogId;

    @Indexed(unique = true)
    private String projectId;

    private LocalTime startTime;
    private LocalTime endTime;
    private String timeZone;
    private boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
