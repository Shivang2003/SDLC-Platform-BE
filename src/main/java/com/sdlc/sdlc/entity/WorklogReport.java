package com.sdlc.sdlc.entity;

import com.sdlc.sdlc.enums.ReportStatus;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Document(collection = "worklog_reports")
@Data
public class WorklogReport {

    @Id
    private String id;

    private String reportId;

    private String projectId;

    private LocalDate reportDate;

    private LocalTime windowStart;
    private LocalTime windowEnd;

    private List<WorklogReportEntry> worklogs;

    private LocalDateTime generatedAt;

    private ReportStatus status;
}