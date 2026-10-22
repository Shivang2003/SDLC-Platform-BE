package com.sdlc.sdlc.entity;

import com.sdlc.sdlc.enums.TestExecutionStatus;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "test_execution_history")
public class TestExecutionHistory {

    @Id
    private String id;

    private String testTicketKey;   // TC-1
    private String devTicketKey;    // DEV-101

    private Integer devTicketRevision; // Revision 2

    private TestExecutionStatus executionStatus;

    private String executedBy;

    private LocalDateTime executedAt;

    private String remarks;
}