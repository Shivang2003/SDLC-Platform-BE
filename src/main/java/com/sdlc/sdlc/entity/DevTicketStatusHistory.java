package com.sdlc.sdlc.entity;

import com.sdlc.sdlc.enums.TicketStatus;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "dev_ticket_status_history")
public class DevTicketStatusHistory {

    @Id
    private ObjectId id;

    @DBRef
    private DevTicket devTicket;

    private TicketStatus previousStatus;
    private TicketStatus newStatus;

    @DBRef
    private ProjectMember changedBy;

    private LocalDateTime changedAt;
    private String comment;
}
