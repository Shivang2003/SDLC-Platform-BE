package com.sdlc.sdlc.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "test_ticket_bug_ticket_links")
public class TestTicketBugTicketLink {

    @Id
    private String id;

    private String testTicketKey;
    private String bugTicketKey;

    private LocalDateTime createdAt;
}