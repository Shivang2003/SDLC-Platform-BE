package com.sdlc.sdlc.dto;

import lombok.Data;

import java.time.LocalTime;

@Data
public class UpdateWorklogConfigRequest {
    private LocalTime startTime;
    private LocalTime endTime;
    private String timeZone;
}
