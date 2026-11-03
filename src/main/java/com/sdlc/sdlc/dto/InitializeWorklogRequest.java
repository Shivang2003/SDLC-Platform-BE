package com.sdlc.sdlc.dto;

import lombok.Data;

import java.time.LocalTime;

@Data
public class InitializeWorklogRequest {

    private LocalTime startTime;

    private LocalTime endTime;

    private String timeZone;
}