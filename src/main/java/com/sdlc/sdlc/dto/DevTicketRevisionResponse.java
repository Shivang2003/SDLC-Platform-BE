package com.sdlc.sdlc.dto;

import com.sdlc.sdlc.entity.Feature;
import com.sdlc.sdlc.entity.FieldChange;
import com.sdlc.sdlc.enums.Priority;
import com.sdlc.sdlc.enums.TicketStatus;
import lombok.Data;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class DevTicketRevisionResponse {

    private String ticketKey;
    private Integer revisionNumber;
    private String title;
    private String description;
    private String projectId;
    private String featureId;
    private String assigneeUserName;
    private String createdByUserName;
    private TicketStatus status;
    private Priority priority;
    private List<String> changedFields;
    private Map<String, FieldChange> changes;
    private String changedByUserName;
    private LocalDateTime createdAt;
}