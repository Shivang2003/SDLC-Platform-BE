package com.sdlc.sdlc.entity;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "bug_tickets")
public class BugTicket {

    @Id
    private ObjectId id;

    private String bugId;
    private String title;
    private String description;

    private String status;
    private String priority;

    @DBRef
    private DevTicket devTicket;

    @DBRef
    private TestCase testCase;

    @DBRef
    private User assignedTo;

    @DBRef
    private User createdBy;
}
