package com.sdlc.sdlc.entity;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Document(collection = "test_case_dev_ticket_links")
public class TestCaseDevTicketLink {

    @Id
    private ObjectId id;

    private ObjectId testCaseId;
    private ObjectId devTicketId;

    private LocalDateTime createdAt;
}
