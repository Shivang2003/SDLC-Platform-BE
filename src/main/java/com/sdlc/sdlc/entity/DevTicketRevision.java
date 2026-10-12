package com.sdlc.sdlc.entity;

import com.sdlc.sdlc.enums.Priority;
import com.sdlc.sdlc.enums.TicketStatus;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "dev_ticket_revisions")
public class DevTicketRevision {

    @Id
    private ObjectId id;

    private String ticketKey;
    private Integer revisionNumber;

    @DBRef
    private DevTicket devTicket;

    private String title;
    private String description;

    @DBRef
    private Project project;

    @DBRef
    private Feature feature;

    @DBRef
    private ProjectMember assignee;

    @DBRef
    private ProjectMember createdBy;

    private TicketStatus status;
    private Priority priority;

    private ObjectId targetVersionId;

    @DBRef
    private ProjectMember changedBy;

    private LocalDateTime createdAt;
}
