package com.sdlc.sdlc.entity;

import com.sdlc.sdlc.enums.Priority;
import com.sdlc.sdlc.enums.TicketStatus;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "dev_ticket_versions")
public class DevTicketVersion {

    @Id
    private ObjectId id;

    private ObjectId devTicketId;

    private int revision;

    private String title;
    private String description;

    private TicketStatus status;
    private Priority priority;

    private ObjectId assigneeId;

    private ObjectId projectVersionId;

    private ObjectId changedBy;

    private LocalDateTime createdAt;
}
