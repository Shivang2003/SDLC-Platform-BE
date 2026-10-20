package com.sdlc.sdlc.entity;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "test_ticket_revision_history")
public class TestTicketRevisionHistory {

    @Id
    private ObjectId id;

    @DBRef
    private TestTicket testTicket;

    private Integer previousRevision;
    private Integer selectedRevision;

    @DBRef
    private ProjectMember changedBy;

    private LocalDateTime changedAt;
    private String reason;
}