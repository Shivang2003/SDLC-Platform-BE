package com.sdlc.sdlc.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class ProjectWorklogConfigResponse {
    private String workLogId;
    private String projectId;
    private LocalTime startTime;
    private LocalTime endTime;
    private String timeZone;
    private boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}