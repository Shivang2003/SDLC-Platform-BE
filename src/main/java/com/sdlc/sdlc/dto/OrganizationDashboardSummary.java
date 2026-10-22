package com.sdlc.sdlc.dto;

import lombok.Data;

@Data
public class OrganizationDashboardSummary {

    private long totalProjects;
    private long totalFeatures;

    private long totalDevTickets;
    private long completedDevTickets;

    private long totalTestTickets;
    private long passedTests;
    private long failedTests;

    private long totalBugTickets;
    private long openBugs;
}