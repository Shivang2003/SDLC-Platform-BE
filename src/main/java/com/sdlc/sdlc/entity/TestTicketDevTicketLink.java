package com.sdlc.sdlc.entity;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Data
@Document(collection = "test_ticket_dev_ticket_links")
public class TestTicketDevTicketLink {

    @Id
    private ObjectId id;

    private String testCaseId;
    private String devTicketId;

    private LocalDateTime createdAt;
}
