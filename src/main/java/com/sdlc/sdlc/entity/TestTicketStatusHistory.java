package com.sdlc.sdlc.entity;

import com.sdlc.sdlc.enums.TestCaseStatus;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "test_ticket_status_history")
public class TestTicketStatusHistory {

    @Id
    private ObjectId id;

    @DBRef
    private TestTicket testTicket;

    private TestCaseStatus previousStatus;
    private TestCaseStatus newStatus;

    @DBRef
    private ProjectMember changedBy;

    private LocalDateTime changedAt;
    private String comment;
}